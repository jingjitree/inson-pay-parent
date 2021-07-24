package top.inson.springboot.notify.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.http.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import top.inson.springboot.data.dao.IPayOrderMapper;
import top.inson.springboot.data.dao.IRefundOrderMapper;
import top.inson.springboot.data.entity.MerCashier;
import top.inson.springboot.data.entity.PayOrder;
import top.inson.springboot.data.entity.RefundOrder;
import top.inson.springboot.data.enums.PayOrderStatusEnum;
import top.inson.springboot.data.enums.RefundStatusEnum;
import top.inson.springboot.data.enums.SignTypeEnum;
import top.inson.springboot.notify.constant.RabbitmqConstant;
import top.inson.springboot.notify.mq.MqSender;
import top.inson.springboot.notify.service.IBaseNotifyService;
import top.inson.springboot.paycommon.entity.dto.PayNotifyDto;
import top.inson.springboot.paycommon.entity.dto.RefundNotifyDto;
import top.inson.springboot.paycommon.service.IPayCacheService;
import top.inson.springboot.utils.AmountUtil;
import top.inson.springboot.utils.HttpUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Service
public class BaseNotifyServiceImpl implements IBaseNotifyService {
    @Autowired
    private IPayOrderMapper payOrderMapper;
    @Autowired
    private IRefundOrderMapper refundOrderMapper;


    @Autowired
    private IPayCacheService payCacheService;


    @Autowired
    private MqSender mqSender;
    @Autowired
    private RabbitmqConstant mqConstant;


    private final Gson gson = new GsonBuilder().create();
    @Override
    public void payOrderNotify(PayOrder upOrder, String orderNo) {
        Example example = new Example(PayOrder.class);
        example.createCriteria()
                .andEqualTo("orderNo", orderNo);

        PayOrder payOrder = payOrderMapper.selectOneByExample(example);
        if (payOrder == null){
            log.info("找不到改订单orderNo：" + orderNo);
            return;
        }
        PayOrderStatusEnum category = PayOrderStatusEnum.getCategory(payOrder.getOrderStatus());
        //部分退款或全额退款的订单，支付接口不更新状态
        if (category != PayOrderStatusEnum.PARTIAL_REFUND
                && category != PayOrderStatusEnum.FULL_REFUND) {
            //更新支付订单
            if (StrUtil.isNotBlank(upOrder.getOrderNo()))
                upOrder.setOrderNo(null);
            log.info("更新订单参数upOrder: {}", gson.toJson(upOrder));
            BeanUtil.copyProperties(upOrder, payOrder,
                    CopyOptions.create().setIgnoreNullValue(true)
            );
            payOrderMapper.updateByExampleSelective(upOrder, example);
        }
        //通知下游商户
        this.notifyPayOrderDown(payOrder);
    }

    private void notifyPayOrderDown(PayOrder payOrder) {
        String notifyUrl = payOrder.getNotifyUrl();
        MerCashier merCashier = payCacheService.getCashier(payOrder.getCashier());
        PayNotifyDto notifyDto = new PayNotifyDto();
        BeanUtil.copyProperties(payOrder, notifyDto);
        notifyDto.setChOrderNo(null)
                .setPreChOrderNo(null)
                .setPayMoney(AmountUtil.changeYuanToFen(payOrder.getPayAmount()));
        String notifyJson = gson.toJson(notifyDto);

        Map<String, String> headers = null;
        HttpResponse response = null;
        try {
            headers = this.buildNotifyHeader(notifyJson, merCashier);
            response = HttpUtils.sendPostJson(notifyUrl, headers, notifyJson);
        } catch (Exception e) {
            log.error("通知下游异常", e);
        }
        if (response != null && response.isOk()) {
            String body = response.body();
            log.info("下游通知结果：" + body);
            if ("SUCCESS".equalsIgnoreCase(body)) {
                log.info("通知成功orderNo：" + payOrder.getOrderNo());
                return;
            }
        }
        Map<String, Object> mqNotifyMap = MapUtil.builder(new HashMap<String, Object>())
                .put("notifyUrl", notifyUrl)
                .put("data", notifyDto)
                .put("headers", headers)
                .put("notifyCount", 1)
                .build();
        String mqJson = gson.toJson(mqNotifyMap);
        mqSender.send(mqConstant.getPayDelayExchange(), mqConstant.getPayDelayRoutingKey(), mqJson, 1000);
    }

    private Map<String, String> buildNotifyHeader(String notifyJson, MerCashier merCashier) {
        SignTypeEnum signTypeEnum = SignTypeEnum.getCategory(merCashier.getSignType());
        String signParams = notifyJson + "&key=" + merCashier.getSignKey();
        log.info("回调签名参数signParams：{}", signParams);
        //构建回调请求头
        return MapUtil.builder(new HashMap<String, String>())
                .put(Header.CONTENT_TYPE.getValue(), ContentType.JSON.getValue())
                .put("signType", signTypeEnum.getDesc())
                .put("paySign", DigestUtil.md5Hex(signParams).toUpperCase())
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refundNotify(RefundOrder upOrder, String refundNo) {
        Example example = new Example(RefundOrder.class);
        example.createCriteria()
                .andEqualTo("refundNo", refundNo);
        RefundOrder refundOrder = refundOrderMapper.selectOneByExample(example);
        if (refundOrder == null){
            log.info("退款订单不存在refundNo:" + refundNo);
            return;
        }

        if (StrUtil.isNotBlank(upOrder.getRefundNo()))
            upOrder.setRefundNo(null);
        log.info("更新退款订单参数upOrder：{}", gson.toJson(upOrder));
        refundOrderMapper.updateByExampleSelective(upOrder, example);
        if (upOrder.getRefundStatus().equals(RefundStatusEnum.REFUND_SUCCESS.getCode())){
            //更改订单状态
            Example orderExample = new Example(PayOrder.class);
            orderExample.createCriteria()
                    .andEqualTo("orderNo", refundOrder.getPayOrderNo());
            PayOrder payOrder = payOrderMapper.selectOneByExample(orderExample);
            PayOrder upPayOrder = null;
            BigDecimal payAmount = payOrder.getPayAmount();
            BigDecimal allRefundAmount = payOrder.getAllRefundAmount();
            if (allRefundAmount == null)
                allRefundAmount = BigDecimal.ZERO;
            if (allRefundAmount.compareTo(payAmount) < 0){
                upPayOrder = new PayOrder()
                        .setOrderStatus(PayOrderStatusEnum.PARTIAL_REFUND.getCode())
                        .setOrderDesc(PayOrderStatusEnum.PARTIAL_REFUND.getDesc());
            }else if(allRefundAmount.compareTo(payAmount) == 0) {
                upPayOrder = new PayOrder()
                        .setOrderStatus(PayOrderStatusEnum.FULL_REFUND.getCode())
                        .setOrderDesc(PayOrderStatusEnum.FULL_REFUND.getDesc());
            }
            if (upPayOrder != null) {
                log.info("更新支付订单参数upPayOrder：{}", gson.toJson(upPayOrder));
                payOrderMapper.updateByExampleSelective(upPayOrder, orderExample);
            }
        }
        //通知下游
        BeanUtil.copyProperties(upOrder, refundOrder,
                CopyOptions.create().setIgnoreNullValue(true)
        );
        this.notifyRefundOrderDown(refundOrder);
    }

    private void notifyRefundOrderDown(RefundOrder refundOrder) {
        String notifyUrl = refundOrder.getNotifyUrl();
        MerCashier merCashier = payCacheService.getCashier(refundOrder.getCashier());
        RefundNotifyDto notifyDto = new RefundNotifyDto();
        BeanUtil.copyProperties(refundOrder, notifyDto);
        notifyDto
                .setRefundMoney(AmountUtil.changeYuanToFen(refundOrder.getRefundAmount()));
        String notifyJson = gson.toJson(notifyDto);
        Map<String, String> headers = null;
        HttpResponse response = null;
        try {
            headers = this.buildNotifyHeader(notifyJson, merCashier);
            response = HttpUtils.sendPostJson(notifyUrl, headers, notifyJson);
        }catch (Exception e){
            log.error("退款通知异常", e);
        }
        if (response != null && response.isOk()) {
            String body = response.body();
            log.info("下游通知结果：" + body);
            if ("SUCCESS".equalsIgnoreCase(body)) {
                log.info("通知成功refundNo：" + refundOrder.getRefundNo());
                return;
            }
        }
        Map<String, Object> mqNotifyMap = MapUtil.builder(new HashMap<String, Object>())
                .put("notifyUrl", notifyUrl)
                .put("data", notifyDto)
                .put("headers", headers)
                .put("notifyCount", 1)
                .build();
        String mqJson = gson.toJson(mqNotifyMap);
        mqSender.send(mqConstant.getPayDelayExchange(), mqConstant.getRefundDelayRoutingKey(), mqJson, 1000);
    }

}

package top.inson.springboot.notify.service.impl;

import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import top.inson.springboot.data.dao.IPayOrderMapper;
import top.inson.springboot.data.dao.IRefundOrderMapper;
import top.inson.springboot.data.entity.PayOrder;
import top.inson.springboot.data.entity.RefundOrder;
import top.inson.springboot.data.enums.PayOrderStatusEnum;
import top.inson.springboot.data.enums.RefundStatusEnum;
import top.inson.springboot.notify.service.IBaseNotifyService;

import java.math.BigDecimal;


@Slf4j
@Service
public class BaseNotifyServiceImpl implements IBaseNotifyService {
    @Autowired
    private IPayOrderMapper payOrderMapper;
    @Autowired
    private IRefundOrderMapper refundOrderMapper;


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
            payOrderMapper.updateByExampleSelective(upOrder, example);
        }
        //通知下游商户

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
    }

}

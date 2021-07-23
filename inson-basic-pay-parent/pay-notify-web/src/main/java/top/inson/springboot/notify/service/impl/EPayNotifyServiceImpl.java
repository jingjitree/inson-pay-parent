package top.inson.springboot.notify.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import top.inson.springboot.data.dao.IPayOrderMapper;
import top.inson.springboot.data.dao.IRefundOrderMapper;
import top.inson.springboot.data.entity.PayOrder;
import top.inson.springboot.data.entity.RefundOrder;
import top.inson.springboot.data.enums.PayOrderStatusEnum;
import top.inson.springboot.data.enums.RefundStatusEnum;
import top.inson.springboot.notify.service.IBaseNotifyService;
import top.inson.springboot.notify.service.IEPayNotifyService;
import top.inson.springboot.paycommon.constant.EpayConfig;
import top.inson.springboot.paycommon.util.PFXUtil;

import java.nio.charset.Charset;
import java.security.PublicKey;
import java.util.Date;
import java.util.Map;


@Slf4j
@Service
public class EPayNotifyServiceImpl implements IEPayNotifyService {
    @Autowired
    private IPayOrderMapper payOrderMapper;
    @Autowired
    private IRefundOrderMapper refundOrderMapper;

    @Autowired
    private EpayConfig epayConfig;

    @Autowired
    private IBaseNotifyService baseNotifyService;


    private final Gson gson = new GsonBuilder().create();
    @Override
    public String notifyMe(Map<String, Object> notifyMap, String efpsSign) {
        String reqJson = gson.toJson(notifyMap);
        log.info("易票联回调reqJson：{}", reqJson);
        boolean check = this.checkEfpsSign(reqJson, efpsSign);
        if (!check) {
            log.info("验证签名失败");
            return "fail";
        }
        String orderNo = (String) notifyMap.get("outTradeNo");
        String payState = (String) notifyMap.get("payState");
        log.info("订单号orderNo：{},payState:{}", orderNo, payState);
        Example example = new Example(PayOrder.class);
        example.createCriteria()
                .andEqualTo("orderNo", orderNo);
        int countOrder = payOrderMapper.selectCountByExample(example);
        if (countOrder <= 0){
            log.info("orderNo:{},找不到该订单", orderNo);
            return "fail";
        }
        PayOrderStatusEnum orderEnum;
        Date payTime = null;
        switch (payState){
            case "00":
                orderEnum = PayOrderStatusEnum.PAY_SUCCESS;
                try {
                    payTime = DateUtil.parse((String)notifyMap.get("payTime"), DatePattern.PURE_DATETIME_PATTERN);
                }catch (Exception e){
                    payTime = DateUtil.date();
                }
                break;
            case "01":
                orderEnum = PayOrderStatusEnum.PAY_FAIL;
                break;
            case "05":
                orderEnum = PayOrderStatusEnum.PAY_CANCEL;
                break;
            case "06":
                orderEnum = PayOrderStatusEnum.PAY_CANCEL;
                break;
            default:
                orderEnum = PayOrderStatusEnum.PAYING;
                break;
        }

        PayOrder upOrder = new PayOrder()
                .setOrderStatus(orderEnum.getCode())
                .setOrderDesc(orderEnum.getDesc())
                .setPayTime(payTime)
                .setChOrderNo((String) notifyMap.get("transactionNo"))
                .setPreChOrderNo((String) notifyMap.get("channelOrder"))
                .setOpenid((String) notifyMap.get("openId"))
                .setSubOpenid((String) notifyMap.get("subOpenId"));

        baseNotifyService.payOrderNotify(upOrder, orderNo);
        return "SUCCESS";
    }

    @Override
    public String refundNotify(Map<String, Object> notifyMap, String efpsSign) {
        String reqJson = gson.toJson(notifyMap);
        log.info("易票联退款回调json：{}", reqJson);
        boolean check = this.checkEfpsSign(reqJson, efpsSign);
        if (!check) {
            log.info("验证签名失败");
            return "fail";
        }
        String refundNo = (String) notifyMap.get("outRefundNo");
        String refundState = (String) notifyMap.get("payState");
        log.info("退款订单号refundNo：{},退款状态：{}", refundNo, refundState);
        Example example = new Example(RefundOrder.class);
        example.createCriteria()
                .andEqualTo("refundNo", refundNo);
        int countOrder = refundOrderMapper.selectCountByExample(example);
        if (countOrder <= 0){
            log.info("退款订单没找到refundNo:" + refundNo);
            return "fail";
        }
        RefundStatusEnum refundEnum;
        Date refundTime = null;
        switch (refundState){
            case "00":
                refundEnum = RefundStatusEnum.REFUND_SUCCESS;
                refundTime = DateUtil.parse((String)notifyMap.get("payTime"), DatePattern.PURE_DATETIME_PATTERN);
                break;
            case "01":
                refundEnum = RefundStatusEnum.REFUND_FAIL;
                break;
            default:
                refundEnum = RefundStatusEnum.REFUNDING;
                break;
        }

        RefundOrder upOrder = new RefundOrder()
                .setRefundStatus(refundEnum.getCode())
                .setRefundDesc(refundEnum.getDesc())
                .setRefundTime(refundTime)
                .setChRefundNo((String) notifyMap.get("transactionNo"))
                .setPreChRefundNo((String) notifyMap.get("channelTradeNo"));

        baseNotifyService.refundNotify(upOrder, refundNo);
        return "SUCCESS";
    }

    private boolean checkEfpsSign(String reqJson, String efpsSign) {
        String pubCertPath = epayConfig.getPubCertPath();
        log.debug("公钥证书路径：" + pubCertPath);
        try {
            PublicKey publicKey = PFXUtil.getPublicKeyByFilePath(pubCertPath);
            Sign sign = SecureUtil.sign(SignAlgorithm.SHA256withRSA)
                    .setPublicKey(publicKey);
            return sign.verify(reqJson.getBytes(Charset.defaultCharset()), Base64.decodeBase64(efpsSign));
        } catch (Exception e) {
            log.info("回调签名异常", e);
            return false;
        }
    }


}

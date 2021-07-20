package top.inson.springboot.notify.service.impl;

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
import top.inson.springboot.data.entity.PayOrder;
import top.inson.springboot.data.enums.PayOrderStatusEnum;
import top.inson.springboot.notify.service.IBaseNotifyService;
import top.inson.springboot.notify.service.IEPayNotifyService;
import top.inson.springboot.paycommon.constant.EpayConfig;
import top.inson.springboot.paycommon.util.PFXUtil;

import java.nio.charset.Charset;
import java.security.PublicKey;
import java.util.Map;


@Slf4j
@Service
public class EPayNotifyServiceImpl implements IEPayNotifyService {
    @Autowired
    private IPayOrderMapper payOrderMapper;


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
        PayOrderStatusEnum orderStatusEnum;
        switch (payState){
            case "00":
                orderStatusEnum = PayOrderStatusEnum.PAY_SUCCESS;
                break;
            case "01":
                orderStatusEnum = PayOrderStatusEnum.PAY_FAIL;
                break;
            case "05":
                orderStatusEnum = PayOrderStatusEnum.PAY_CANCEL;
                break;
            case "06":
                orderStatusEnum = PayOrderStatusEnum.PAY_CANCEL;
                break;
            default:
                orderStatusEnum = PayOrderStatusEnum.PAYING;
                break;
        }

        PayOrder upOrder = new PayOrder()
                .setOrderStatus(orderStatusEnum.getCode())
                .setOrderDesc(orderStatusEnum.getDesc())
                .setChOrderNo((String) notifyMap.get("transactionNo"))
                .setPreChOrderNo((String) notifyMap.get("channelOrder"))
                .setOpenid((String) notifyMap.get("openId"))
                .setSubOpenid((String) notifyMap.get("subOpenId"));

        baseNotifyService.payOrderNotify(upOrder, orderNo);
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

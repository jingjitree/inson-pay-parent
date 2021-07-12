package top.inson.springboot.pay.service.channel.impl;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import cn.hutool.http.HttpResponse;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;
import top.inson.springboot.common.exception.BadBusinessException;
import top.inson.springboot.data.dao.IPayOrderMapper;
import top.inson.springboot.data.entity.ChannelSubmerConfig;
import top.inson.springboot.data.entity.PayOrder;
import top.inson.springboot.data.enums.PayOrderStatusEnum;
import top.inson.springboot.data.enums.PayTypeEnum;
import top.inson.springboot.pay.annotation.ChannelHandler;
import top.inson.springboot.pay.constant.EpayConfig;
import top.inson.springboot.pay.constant.PayConstant;
import top.inson.springboot.pay.entity.dto.MicroPayDto;
import top.inson.springboot.pay.entity.dto.UnifiedOrderDto;
import top.inson.springboot.pay.enums.PayBadBusinessEnum;
import top.inson.springboot.pay.service.channel.IChannelService;
import top.inson.springboot.utils.AmountUtil;
import top.inson.springboot.utils.HttpUtils;
import top.inson.springboot.utils.PFXUtil;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@ChannelHandler(source = "YPL")
public class EpayServiceImpl implements IChannelService {
    @Autowired
    private IPayOrderMapper payOrderMapper;


    @Autowired
    private EpayConfig epayConfig;



    private final Gson gson = new GsonBuilder().create();
    @Override
    public UnifiedOrderDto unifiedOrder(PayOrder payOrder, ChannelSubmerConfig submerConfig) {
        log.debug("易票联渠道主扫处理的逻辑");
        //支付金额（单位：分）
        String nowDateStr = DateUtil.format(DateUtil.date(), DatePattern.PURE_DATETIME_PATTERN);
        Integer amount = AmountUtil.changeYuanToFen(payOrder.getPayAmount());
        int payMethod;
        switch (PayTypeEnum.getCategory(payOrder.getPayType())){
            case ALIPAY:
                payMethod = 7;
                break;
            default:
                payMethod = 6;
                break;
        }

        List<Map<String, Object>> goodsList = Lists.newArrayList(
                MapUtil.builder(new HashMap<String, Object>())
                        .put("name", payOrder.getBody())
                        .put("number", RandomUtil.randomDouble(1, 10))
                        .put("amount", amount)
                        .build()
        );
        Map<String, Object> orderInfo = MapUtil.builder(new HashMap<String, Object>())
                .put("Id", 1)
                .put("businessType", 100007)
                .put("goodsList", goodsList)
                .build();

        Map<String, Object> reqMap = MapUtil.builder(new HashMap<String, Object>())
                .put("version", "3.0")
                .put("outTradeNo", payOrder.getOrderNo())
                .put("customerCode", submerConfig.getChannelSubMerNo())
                .put("orderInfo", orderInfo)
                .put("payMethod", payMethod)
                .put("payAmount", amount)
                .put("payCurrency", "CNY")
                .put("notifyUrl", "http://g.cn")
                .put("transactionStartTime", nowDateStr)
                .put("nonceStr", RandomUtil.randomString(12))
                .build();
        String reqJson = gson.toJson(reqMap);
        log.info("易票联主扫请求参数：{}", reqJson);
        HttpResponse response = null;
        JsonObject resultJson = new JsonObject();
        try {
            String reqUrl = epayConfig.getBaseUrl() + epayConfig.getUnifiedUrl();
            Map<String, String> headers = this.buildHeadersSign(reqJson, nowDateStr);
            //发送http请求
            response = HttpUtils.sendPostJson(reqUrl, headers, reqJson);
            resultJson.addProperty(PayConstant.STATUS, true);
        } catch (UnsupportedEncodingException e) {
            log.error("编码异常", e);
            resultJson.addProperty(PayConstant.STATUS, false);
        } catch (Exception e) {
            log.error("请求异常", e);
            resultJson.addProperty(PayConstant.STATUS, false);
        }
        return this.doUnifiedResult(payOrder, resultJson, response);
    }

    private UnifiedOrderDto doUnifiedResult(PayOrder payOrder, JsonObject resultJson, HttpResponse response) {
        Example example = new Example(PayOrder.class);
        example.createCriteria()
                .andEqualTo("orderNo", payOrder.getOrderNo());
        if (!resultJson.get(PayConstant.STATUS).getAsBoolean() || !response.isOk()){
            //将订单改为支付失败
            PayOrder upOrder = new PayOrder()
                    .setOrderStatus(PayOrderStatusEnum.CREATE_ORDER_FAIL.getCode())
                    .setOrderDesc("下单失败或请求失败");
            payOrderMapper.updateByExampleSelective(upOrder, example);
            throw new BadBusinessException(PayBadBusinessEnum.BUSINESS_ERROR);
        }
        String body = response.body();
        log.info("主扫响应结果body:{}", body);
        //主扫响应结果body:{"returnCode":"0000","returnMsg":"Success","nonceStr":"2c1653f9142343a9bc2b1c0dec527098","codeUrl":"https://epsp.epaylinks.cn/api/cash/cashier?token=9e0faf56d27a4454a669af2abb86f1a8","outTradeNo":null,"amount":1}
        JsonObject bodyObj = gson.fromJson(body, JsonObject.class);
        String returnMsg = bodyObj.get("returnMsg").getAsString();
        if (!"0000".equals(bodyObj.get("returnCode").getAsString())){
            PayOrder upOrder = new PayOrder()
                    .setOrderStatus(PayOrderStatusEnum.CREATE_ORDER_FAIL.getCode())
                    .setOrderDesc(StrUtil.isBlank(returnMsg) ? "请求渠道下单失败" : returnMsg);
            payOrderMapper.updateByExampleSelective(upOrder, example);
            throw new BadBusinessException(PayBadBusinessEnum.CREATE_ORDER_FAIL);
        }

        //构建返回参数
        UnifiedOrderDto orderDto = new UnifiedOrderDto()
                .setCodeUrl(bodyObj.get("codeUrl").getAsString());
        orderDto.setOrderDesc(returnMsg);
        return orderDto;
    }

    @Override
    public MicroPayDto microPay(PayOrder payOrder, ChannelSubmerConfig submerConfig) {
        String nowDateStr = DateUtil.format(DateUtil.date(), DatePattern.PURE_DATETIME_PATTERN);
        Integer amount = AmountUtil.changeYuanToFen(payOrder.getPayAmount());
        int payMethod;
        switch (PayTypeEnum.getCategory(payOrder.getPayType())){
            case ALIPAY:
                payMethod = 14;
                break;
            default:
                payMethod = 13;
                break;
        }

        List<Map<String, Object>> goodsList = Lists.newArrayList(
                MapUtil.builder(new HashMap<String, Object>())
                        .put("name", "测试商品")
                        .put("number", "0.75")
                        .put("amount", 1)
                        .build()
        );
        Map<String, Object> orderInfo = MapUtil.builder(new HashMap<String, Object>())
                .put("Id", 1)
                .put("businessType", 100007)
                .put("goodsList", goodsList)
                .build();

        Map<String, Object> reqMap = MapUtil.builder(new HashMap<String, Object>())
                .put("outTradeNo", payOrder.getOrderNo())
                .put("customerCode", submerConfig.getChannelSubMerNo())
                .put("orderInfo", orderInfo)
                .put("payMethod", payMethod)
                .put("payAmount", amount)
                .put("payCurrency","CNY")
                .put("transactionStartTime", nowDateStr)
                .put("authCode", payOrder.getAuthCode())
                .put("nonceStr", RandomUtil.randomString(12))
                .build();
        String reqJson = gson.toJson(reqMap);
        HttpResponse response = null;
        JsonObject resultJson = new JsonObject();
        try {
            String reqUrl = epayConfig.getBaseUrl() + epayConfig.getMicroPayUrl();
            Map<String, String> headers = this.buildHeadersSign(reqJson, nowDateStr);
            response = HttpUtils.sendPostJson(reqUrl, headers, reqJson);
            resultJson.addProperty(PayConstant.STATUS, true);
        } catch (Exception e) {
            log.error("被扫请求异常", e);
            resultJson.addProperty(PayConstant.STATUS, false);
        }
        return this.doMicroPayResult(payOrder, resultJson, response);
    }

    private MicroPayDto doMicroPayResult(PayOrder payOrder, JsonObject resultJson, HttpResponse response) {
        Example example = new Example(PayOrder.class);
        example.createCriteria()
                .andEqualTo("orderNo", payOrder.getOrderNo());
        if (!resultJson.get(PayConstant.STATUS).getAsBoolean() || !response.isOk()){
            //将订单改为支付失败
            PayOrder upOrder = new PayOrder()
                    .setOrderStatus(PayOrderStatusEnum.CREATE_ORDER_FAIL.getCode())
                    .setOrderDesc("下单失败或请求失败");
            payOrderMapper.updateByExampleSelective(upOrder, example);
            throw new BadBusinessException(PayBadBusinessEnum.BUSINESS_ERROR);
        }
        String body = response.body();
        log.info("被扫响应结果body:{}", body);
        JsonObject bodyObj = gson.fromJson(body, JsonObject.class);
        String returnMsg = bodyObj.get("returnMsg").getAsString();
        if (!"0000".equals(bodyObj.get("returnCode").getAsString())){
            PayOrder upOrder = new PayOrder()
                    .setOrderStatus(PayOrderStatusEnum.CREATE_ORDER_FAIL.getCode())
                    .setOrderDesc(StrUtil.isBlank(returnMsg) ? "请求渠道下单失败" : returnMsg);
            payOrderMapper.updateByExampleSelective(upOrder, example);
            throw new BadBusinessException(PayBadBusinessEnum.CREATE_ORDER_FAIL);
        }
        String payState = bodyObj.get("payState").getAsString();
        Integer orderStatus = PayOrderStatusEnum.PAYING.getCode();
        switch (payState){
            case "00":
                orderStatus = PayOrderStatusEnum.PAY_SUCCESS.getCode();
                break;
            case "01":
                orderStatus = PayOrderStatusEnum.PAY_FAIL.getCode();
                break;
            case "03":
                orderStatus = PayOrderStatusEnum.PAYING.getCode();
                break;
            default:
                orderStatus = PayOrderStatusEnum.PAY_CANCEL.getCode();
                break;
        }
        MicroPayDto payDto = new MicroPayDto();
        payDto.setOrderStatus(orderStatus)
                .setOrderDesc(returnMsg);
        return payDto;
    }

    @Override
    public void refundOrder() {

    }

    @Override
    public void orderQuery() {

    }

    @Override
    public void refundQuery() {

    }

    private Map<String, String> buildHeadersSign(String reqJson, String nowDateStr) throws Exception{
        Map<String, String> headers = MapUtil.builder(new HashMap<String, String>())
                .put("x-efps-sign-no", epayConfig.getCertNo())
                .put("x-efps-sign-type", epayConfig.getSignType())
                .put("x-efps-timestamp", nowDateStr)
                .build();
        //计算签名
        Sign sign = SecureUtil.sign(SignAlgorithm.SHA256withRSA)
                .setPrivateKey(PFXUtil.getPrivateKeyByPfx(epayConfig.getCertPath(), epayConfig.getCertPwd()));
        byte[] signByte = sign.sign(reqJson.getBytes(Charset.defaultCharset()));
        headers.put("x-efps-sign", Base64Encoder.encode(signByte));
        return headers;
    }
}

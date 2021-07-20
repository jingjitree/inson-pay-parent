package top.inson.springboot.pay.service.channel.impl;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import cn.hutool.http.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;
import top.inson.springboot.common.exception.BadBusinessException;
import top.inson.springboot.data.dao.IPayOrderMapper;
import top.inson.springboot.data.dao.IRefundOrderMapper;
import top.inson.springboot.data.entity.ChannelSubmerConfig;
import top.inson.springboot.data.entity.PayOrder;
import top.inson.springboot.data.entity.RefundOrder;
import top.inson.springboot.data.enums.PayOrderStatusEnum;
import top.inson.springboot.data.enums.PayTypeEnum;
import top.inson.springboot.data.enums.RefundStatusEnum;
import top.inson.springboot.pay.annotation.ChannelHandler;
import top.inson.springboot.paycommon.constant.PayConfig;
import top.inson.springboot.pay.constant.PayConstant;
import top.inson.springboot.pay.entity.dto.*;
import top.inson.springboot.pay.enums.PayBadBusinessEnum;
import top.inson.springboot.pay.service.channel.IChannelService;
import top.inson.springboot.paycommon.constant.EpayConfig;
import top.inson.springboot.utils.AmountUtil;
import top.inson.springboot.utils.HttpUtils;
import top.inson.springboot.paycommon.util.PFXUtil;

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
    private IRefundOrderMapper refundOrderMapper;


    @Autowired
    private EpayConfig epayConfig;
    @Autowired
    private PayConfig payConfig;



    private final Gson gson = new GsonBuilder()
            .create();
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
        Map<String, Object> orderInfo = this.buildOrderInfo(payOrder);

        Map<String, Object> reqMap = MapUtil.builder(new HashMap<String, Object>())
                .put("version", "3.0")
                .put("outTradeNo", payOrder.getOrderNo())
                .put("customerCode", submerConfig.getChannelSubMerNo())
                .put("orderInfo", orderInfo)
                .put("payMethod", payMethod)
                .put("payAmount", amount)
                .put("payCurrency", "CNY")
                .put("notifyUrl", payConfig.getPayBaseUrl() + payConfig.getENotifyUrl())
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
        //返回参数
        UnifiedOrderDto orderDto = new UnifiedOrderDto();
        if (!"0000".equals(bodyObj.get("returnCode").getAsString())){
            orderDto.setOrderStatus(PayOrderStatusEnum.CREATE_ORDER_FAIL.getCode())
                    .setOrderDesc(StrUtil.isBlank(returnMsg) ? "请求渠道下单失败" : returnMsg);
        }else {
            //构建返回参数
            orderDto.setCodeUrl(bodyObj.get("codeUrl").getAsString())
                    .setOrderStatus(PayOrderStatusEnum.PAYING.getCode())
                    .setOrderDesc(returnMsg);
        }
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
        Map<String, Object> orderInfo = this.buildOrderInfo(payOrder);

        Map<String, Object> reqMap = MapUtil.builder(new HashMap<String, Object>())
                .put("outTradeNo", payOrder.getOrderNo())
                .put("customerCode", submerConfig.getChannelSubMerNo())
                .put("orderInfo", orderInfo)
                .put("payMethod", payMethod)
                .put("payAmount", amount)
                .put("payCurrency","CNY")
                .put("notifyUrl", payConfig.getPayBaseUrl() + payConfig.getENotifyUrl())
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
        //返回参数
        MicroPayDto payDto = new MicroPayDto();
        if (!"0000".equals(bodyObj.get("returnCode").getAsString())){
            payDto.setOrderStatus(PayOrderStatusEnum.CREATE_ORDER_FAIL.getCode())
                    .setOrderDesc(StrUtil.isBlank(returnMsg) ? "请求渠道下单失败" : returnMsg);
        }else {
            String payState = bodyObj.get("payState").getAsString();
            int orderStatus;
            switch (payState) {
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
            payDto.setOrderStatus(orderStatus)
                    .setOrderDesc(returnMsg);
        }
        return payDto;
    }

    @Override
    public RefundOrderDto refundOrder(RefundOrder refundOrder, ChannelSubmerConfig submerConfig) {
        String nowDateStr = DateUtil.format(DateUtil.date(), DatePattern.PURE_DATETIME_PATTERN);
        Example orderExample = new Example(PayOrder.class);
        orderExample.createCriteria()
                .andEqualTo("orderNo", refundOrder.getPayOrderNo());
        PayOrder payOrder = payOrderMapper.selectOneByExample(orderExample);

        Map<String, Object> reqMap = MapUtil.builder(new HashMap<String, Object>())
                .put("customerCode", submerConfig.getChannelSubMerNo())
                .put("outRefundNo", RandomUtil.randomNumbers(13))
                .put("outTradeNo", payOrder.getOrderNo())
                .put("refundAmount", AmountUtil.changeYuanToFen(refundOrder.getRefundAmount()))
                .put("amount", AmountUtil.changeYuanToFen(payOrder.getPayAmount()))
                .put("notifyUrl", payConfig.getPayBaseUrl() + payConfig.getERefundNotifyUrl())
                .put("nonceStr", RandomUtil.randomString(12))
                .build();
        if (StrUtil.isNotBlank(refundOrder.getRemark()))
            reqMap.put("remark", refundOrder.getRemark());
        String reqJson = gson.toJson(reqMap);
        JsonObject resultJson = new JsonObject();
        HttpResponse response = null;
        try {
            String reqUrl = epayConfig.getBaseUrl() + epayConfig.getRefundOrderUrl();
            Map<String, String> headers = this.buildHeadersSign(reqJson, nowDateStr);
            response = HttpUtils.sendPostJson(reqUrl, headers, reqJson);
            resultJson.addProperty(PayConstant.STATUS, true);
        }catch (Exception e){
            log.error("易票联退款异常", e);
            resultJson.addProperty(PayConstant.STATUS, false);
        }

        return this.doRefundOrderResult(refundOrder, resultJson, response);
    }

    private RefundOrderDto doRefundOrderResult(RefundOrder refundOrder, JsonObject resultJson, HttpResponse response) {
        Example example = new Example(RefundOrder.class);
        example.createCriteria()
                .andEqualTo("refundNo", refundOrder.getRefundNo());
        if (!resultJson.get(PayConstant.STATUS).getAsBoolean() || !response.isOk()){
            //将订单改为支付失败
            RefundOrder upOrder = new RefundOrder()
                    .setRefundStatus(RefundStatusEnum.REFUND_FAIL.getCode())
                    .setRefundDesc("退款失败或请求失败");
            refundOrderMapper.updateByExampleSelective(upOrder, example);
            throw new BadBusinessException(PayBadBusinessEnum.BUSINESS_ERROR);
        }
        String body = response.body();
        log.info("退款响应body: {}", body);
        JsonObject bodyObj = gson.fromJson(body, JsonObject.class);
        String returnMsg = bodyObj.get("returnMsg").getAsString();
        //返回参数
        RefundOrderDto orderDto = new RefundOrderDto();
        if (!"0000".equals(bodyObj.get("returnCode").getAsString())){
            orderDto.setRefundStatus(RefundStatusEnum.REFUND_FAIL.getCode())
                    .setRefundDesc(StrUtil.isBlank(returnMsg) ? "请求退款失败" : returnMsg);
        }else {
            orderDto.setRefundStatus(RefundStatusEnum.REFUNDING.getCode())
                    .setRefundDesc(RefundStatusEnum.REFUNDING.getDesc());
        }
        return orderDto;
    }

    @Override
    public OrderQueryDto orderQuery(PayOrder payOrder, ChannelSubmerConfig submerConfig) {
        String nowDateStr = DateUtil.format(DateUtil.date(), DatePattern.PURE_DATETIME_PATTERN);
        Map<String, Object> reqMap = MapUtil.builder(new HashMap<String, Object>())
                .put("customerCode", submerConfig.getChannelSubMerNo())
                .put("outTradeNo", payOrder.getOrderNo())
                .put("nonceStr", RandomUtil.randomString(12))
                .build();
        String reqJson = gson.toJson(reqMap);
        JsonObject resultJson = new JsonObject();
        HttpResponse response = null;
        try {
            String reqUrl = epayConfig.getBaseUrl() + epayConfig.getOrderQueryUrl();
            Map<String, String> headers = this.buildHeadersSign(reqJson, nowDateStr);
            response = HttpUtils.sendPostJson(reqUrl, headers, reqJson);
            resultJson.addProperty(PayConstant.STATUS, true);
        } catch (Exception e) {
            log.error("订单查询异常", e);
            resultJson.addProperty(PayConstant.STATUS, false);
        }
        return this.doOrderQueryResult(resultJson, response);
    }

    private OrderQueryDto doOrderQueryResult(JsonObject resultJson, HttpResponse response) {
        if (!resultJson.get(PayConstant.STATUS).getAsBoolean() || !response.isOk())
            throw new BadBusinessException(PayBadBusinessEnum.SEND_REQUEST_ERROR);

        String body = response.body();
        log.info("易票联订单查询结果：{}", body);
        JsonObject bodyObj = gson.fromJson(body, JsonObject.class);
        String returnMsg = bodyObj.get("returnMsg").getAsString();
        //返回参数
        OrderQueryDto queryDto = new OrderQueryDto();
        if ("0000".equals(bodyObj.get("returnCode").getAsString())){
            int orderStatus;
            switch (bodyObj.get("payState").getAsString()) {
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
            JsonElement transElement = bodyObj.get("transactionNo");
            JsonElement channelElement = bodyObj.get("channelOrder");
            queryDto.setOrderStatus(orderStatus)
                    .setChOrderNo(transElement.isJsonNull() ? null : transElement.getAsString())
                    .setPreChOrderNo(channelElement.isJsonNull() ? null : channelElement.getAsString());
        }
        queryDto.setOrderDesc(returnMsg);
        return queryDto;
    }

    @Override
    public RefundQueryDto refundQuery(RefundOrder refundOrder, ChannelSubmerConfig submerConfig) {
        String nowDateStr = DateUtil.format(DateUtil.date(), DatePattern.PURE_DATETIME_PATTERN);
        Map<String, Object> reqMap = MapUtil.builder(new HashMap<String, Object>())
                .put("customerCode", submerConfig.getChannelSubMerNo())
                .put("outRefundNo", refundOrder.getRefundNo())
                .put("nonceStr", RandomUtil.randomString(12))
                .build();
        String reqJson = gson.toJson(reqMap);
        JsonObject resultJson = new JsonObject();
        HttpResponse response = null;
        try {
            String reqUrl = epayConfig.getBaseUrl() + epayConfig.getRefundQueryUrl();
            Map<String, String> headers = this.buildHeadersSign(reqJson, nowDateStr);
            response = HttpUtils.sendPostJson(reqUrl, headers, reqJson);
            resultJson.addProperty(PayConstant.STATUS, true);
        } catch (Exception e) {
            log.error("退款查询异常", e);
            resultJson.addProperty(PayConstant.STATUS, false);
        }
        return this.doRefundQueryResult(resultJson, response);
    }

    private RefundQueryDto doRefundQueryResult(JsonObject resultJson, HttpResponse response) {
        if (!resultJson.get(PayConstant.STATUS).getAsBoolean() || !response.isOk())
            throw new BadBusinessException(PayBadBusinessEnum.SEND_REQUEST_ERROR);
        String body = response.body();
        log.info("易票联退款查询结果：{}", body);
        JsonObject bodyObj = gson.fromJson(body, JsonObject.class);
        String returnMsg = bodyObj.get("returnMsg").getAsString();
        RefundQueryDto queryDto = new RefundQueryDto();
        if ("0000".equals(bodyObj.get("returnCode").getAsString())){
            int refundStatus;
            switch (bodyObj.get("refundState").getAsString()){
                case "00":
                    refundStatus = RefundStatusEnum.REFUND_SUCCESS.getCode();
                    queryDto.setRefundTime(DateUtil.parse(bodyObj.get("refundTime").getAsString(), DatePattern.PURE_DATETIME_PATTERN));
                    break;
                case "01":
                    refundStatus = RefundStatusEnum.REFUND_FAIL.getCode();
                    break;
                default:
                    refundStatus = RefundStatusEnum.REFUNDING.getCode();
                    break;
            }
            queryDto.setRefundStatus(refundStatus);
        }
        queryDto.setRefundDesc(returnMsg);
        return queryDto;
    }

    /**
     * 构建订单信息
     * @param payOrder
     * @return
     */
    private Map<String, Object> buildOrderInfo(PayOrder payOrder) {
        List<Map<String, Object>> goodsList = CollUtil.newArrayList(
                MapUtil.builder(new HashMap<String, Object>())
                        .put("name", payOrder.getBody())
                        .put("number", RandomUtil.randomDouble(1, 10))
                        .put("amount", AmountUtil.changeYuanToFen(payOrder.getPayAmount()))
                        .build()
        );
        return MapUtil.builder(new HashMap<String, Object>())
                .put("Id", 1)
                .put("businessType", 100007)
                .put("goodsList", goodsList)
                .build();
    }

    /**
     * 构建接口请求头并结算签名
     * @param reqJson
     * @param nowDateStr
     * @return
     * @throws Exception
     */
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

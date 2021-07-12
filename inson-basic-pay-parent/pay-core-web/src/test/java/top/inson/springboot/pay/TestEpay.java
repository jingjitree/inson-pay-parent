package top.inson.springboot.pay;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import cn.hutool.http.HttpResponse;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import top.inson.springboot.utils.HttpUtils;
import top.inson.springboot.utils.PFXUtil;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
public class TestEpay {

    private Gson gson = new GsonBuilder().create();

    private String certNo = "562122003292960001";
    private String certPwd = "ttspay1608";
    private String certPath = "D:/home/certs/epaylink/user-rsa.pfx";
    private String customerCode = "562122003292960";

    @Test
    public void unifiedOrder(){
        String baseUrl = "https://efps.epaylinks.cn";
        String reqUrl = baseUrl + "/api/txs/pay/NativePayment";
        String nowDateStr = DateUtil.format(DateUtil.date(), DatePattern.PURE_DATETIME_PATTERN);

        String orderNo = nowDateStr + RandomUtil.randomNumbers(9);
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
                .put("version", "3.0")
                .put("outTradeNo", orderNo)
                .put("customerCode", customerCode)
                .put("orderInfo", orderInfo)
                .put("payMethod", 6)
                .put("payAmount", 1)
                .put("payCurrency", "CNY")
                .put("notifyUrl", "http://g.cn")
                .put("transactionStartTime", nowDateStr)
                .put("nonceStr", RandomUtil.randomString(12))
                .build();
        String reqJson = gson.toJson(reqMap);
        log.info("主扫请求参数：{}", reqJson);
        try {
            Map<String, String> headers = this.buildHeadersSign(reqJson, nowDateStr);

            HttpResponse response = HttpUtils.sendPostJson(reqUrl, headers, reqJson);
            log.info("主扫响应参数：{}", response.body());
        } catch (UnsupportedEncodingException e) {
            log.error("编码异常", e);
        } catch (Exception e) {
            log.error("签名异常", e);
        }
    }

    @Test
    public void microPay(){
        String baseUrl = "https://efps.epaylinks.cn";
        String reqUrl = baseUrl + "/api/txs/pay/MicroPayment";
        String nowDateStr = DateUtil.format(DateUtil.date(), DatePattern.PURE_DATETIME_PATTERN);

        String orderNo = RandomUtil.randomNumbers(18);
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
                .put("outTradeNo", orderNo)
                .put("customerCode", customerCode)
                .put("orderInfo", orderInfo)
                .put("payMethod", 13)
                .put("payAmount", 1)
                .put("payCurrency","CNY")
                .put("transactionStartTime", nowDateStr)
                .put("authCode", "135623143117295260")
                .put("nonceStr", RandomUtil.randomString(12))
                .build();
        String reqJson = gson.toJson(reqMap);
        try {
            Map<String, String> headers = this.buildHeadersSign(reqJson, nowDateStr);
            HttpResponse response = HttpUtils.sendPostJson(reqUrl, headers, reqJson);
            log.info("被扫响应结果：{}", response.body());
            //{"returnCode":"0227","returnMsg":"AUTH_CODE_INVALID-101 每个二维码仅限使用一次，请刷新再试","nonceStr":null,"outTradeNo":"560541143969225192","transactionNo":null,"amount":null,"procedureFee":null,"payState":null,"payTime":null,"payerId":null,"channelOrder":null,"openId":null,"buyerLogonId":null,"buyerId":null,"cashAmount":null,"couponAmount":null,"payerInfo":null,"promotionDetail":null,"subOpenId":null}
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Map<String, String> buildHeadersSign(String reqJson, String nowDateStr) throws Exception{
        Map<String, String> headers = MapUtil.builder(new HashMap<String, String>())
                .put("x-efps-sign-no", certNo)
                .put("x-efps-sign-type", "SHA256withRSA")
                .put("x-efps-timestamp", nowDateStr)
                .build();
        //计算签名
        Sign sign = SecureUtil.sign(SignAlgorithm.SHA256withRSA)
                .setPrivateKey(PFXUtil.getPrivateKeyByPfx(certPath, certPwd));
        byte[] signByte = sign.sign(reqJson.getBytes(Charset.defaultCharset()));
        headers.put("x-efps-sign", Base64Encoder.encode(signByte));
        return headers;
    }
}

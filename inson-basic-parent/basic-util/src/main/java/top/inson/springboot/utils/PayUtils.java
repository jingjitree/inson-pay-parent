package top.inson.springboot.utils;

import cn.hutool.core.util.StrUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

public class PayUtils {
    public static final String WECHAT = "WECHAT";
    public static final String ALIPAY = "ALIPAY";
    public static final List<String> WECHAT_CODE = Arrays.asList("10", "11", "12", "13", "14", "15");
    public static final List<String> ALIPAY_CODE = Arrays.asList("25", "26", "27", "28", "29", "30");

    public static String getClientTypeByCode(String authCode){
        if (StrUtil.isBlank(authCode))
            return null;
        authCode = authCode.substring(0, 2);
        if (WECHAT_CODE.contains(authCode))
            return WECHAT;
        else if (ALIPAY_CODE.contains(authCode))
            return ALIPAY;
        return null;
    }

    public static String getClientType(HttpServletRequest request){
        String userAgent = request.getHeader("user-agent");
        if (StrUtil.isBlank(userAgent))
            return null;
        if (userAgent.contains("AlipayClient"))
            return ALIPAY;
        else if (userAgent.contains("MicroMessenger"))
            return WECHAT;
        return null;
    }


}

package top.inson.springboot.utils;

import cn.hutool.core.map.MapUtil;
import cn.hutool.http.*;
import cn.hutool.http.ssl.DefaultTrustManager;
import cn.hutool.http.ssl.SSLSocketFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;


@Slf4j
public class HttpUtils extends HttpUtil {


    /**
     * 发送post请求
     * @param reqUrl
     * @param headers
     * @param reqJson
     * @return
     */
    public static HttpResponse sendPostJson(String reqUrl, Map<String, String> headers, String reqJson) throws Exception{
        log.info("请求地址reqUrl: {}", reqUrl);
        HttpRequest request = createPost(reqUrl);
        if (isHttps(reqUrl))
            request.setSSLSocketFactory(
                    SSLSocketFactoryBuilder.create()
                            .setTrustManagers(new DefaultTrustManager()).build()
            );
        if (MapUtil.isEmpty(headers)){
            headers = MapUtil.of(Header.CONTENT_TYPE.getValue(), ContentType.JSON.getValue());
        }
        log.info("请求头headers：{}", headers);
        log.info("请求参数reqJson：{}", reqJson);
        return request.headerMap(headers, true)
                .body(reqJson).execute();
    }


}

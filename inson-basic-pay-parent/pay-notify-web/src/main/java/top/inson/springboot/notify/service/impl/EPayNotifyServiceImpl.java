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
    private EpayConfig epayConfig;

    private final Gson gson = new GsonBuilder().create();
    @Override
    public String notifyMe(Map<String, Object> params, String efpsSign) {
        String reqJson = gson.toJson(params);
        log.info("易票联回调reqJson：{}", reqJson);
        boolean check = this.checkEfpsSign(reqJson, efpsSign);
        if (!check)
            return "fail";
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

package top.inson.springboot.notify;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import top.inson.springboot.paycommon.util.PFXUtil;

import java.security.PublicKey;


@Slf4j
public class TestEpay {

    @Test
    public void verifySign(){
        String path = "D:/home/certs/epaylink/efps.cer";
        try {
            PublicKey publicKey = PFXUtil.getPublicKeyByFilePath(path);
            log.info("pubKey:" + publicKey);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}

package top.inson.springboot.pay;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import top.inson.springboot.paycommon.util.PayUtils;

@Slf4j
public class TestPayUtil {

    @Test
    public void handler(){
        String payType = PayUtils.getClientTypeByCode("28987405");
        log.info(payType);
    }


}

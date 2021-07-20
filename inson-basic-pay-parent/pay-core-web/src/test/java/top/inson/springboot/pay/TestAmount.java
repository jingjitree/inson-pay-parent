package top.inson.springboot.pay;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import top.inson.springboot.data.enums.PayTypeEnum;
import top.inson.springboot.utils.AmountUtil;
import top.inson.springboot.paycommon.util.PayUtils;

@Slf4j
public class TestAmount {

    @Test
    public void changeAmount(){
        Integer amount = 1;
        String yuanAmount = AmountUtil.changeFenToYuan(amount);
        log.info("转换后的金额：" + yuanAmount);

        Integer fenAmount = AmountUtil.changeYuanToFen(yuanAmount);
        log.info("转换成分：" + fenAmount);
    }

    @Test
    public void clientType(){
        String clientType = PayUtils.getClientTypeByCode("28542536");
        log.info("clientType: " + clientType);
        PayTypeEnum en = PayTypeEnum.getCategory(Integer.parseInt(clientType));
        log.info("en枚举：" + en);


    }

}

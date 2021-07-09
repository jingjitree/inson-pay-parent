package top.inson.springboot.pay;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import top.inson.springboot.utils.AmountUtil;

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

}

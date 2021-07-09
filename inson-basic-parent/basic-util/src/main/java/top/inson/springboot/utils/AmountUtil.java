package top.inson.springboot.utils;

import cn.hutool.core.util.StrUtil;

import java.math.BigDecimal;

public class AmountUtil {

    public static final BigDecimal HUNDRED = new BigDecimal(100);


    public static String changeFenToYuan(Integer amount){
        if (amount == null || amount <= 0)
            return "0";
        BigDecimal bigAmount = new BigDecimal(amount);
        return bigAmount.divide(HUNDRED, 2, BigDecimal.ROUND_HALF_UP).toString();
    }

    public static Integer changeYuanToFen(String amount){
        if (StrUtil.isEmpty(amount))
            return null;
        BigDecimal bigAmount = new BigDecimal(amount).setScale(2, BigDecimal.ROUND_HALF_UP);
        return bigAmount.multiply(HUNDRED).intValue();
    }


}

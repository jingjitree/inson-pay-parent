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

    public static Integer changeYuanToFen(Object amount){
        if (amount instanceof String) {
            String strAmount = (String) amount;
            if (StrUtil.isEmpty(strAmount))
                return null;
            BigDecimal bigAmount = new BigDecimal(strAmount).setScale(2, BigDecimal.ROUND_HALF_UP);
            return bigAmount.multiply(HUNDRED).intValue();
        }else if (amount instanceof BigDecimal){
            BigDecimal bigAmount = (BigDecimal) amount;
            return bigAmount.multiply(HUNDRED).intValue();
        }else if (amount instanceof Double){
            Double dobAmount = (Double) amount;
            BigDecimal bigAmount = new BigDecimal(dobAmount).setScale(2, BigDecimal.ROUND_HALF_UP);
            return bigAmount.multiply(HUNDRED).intValue();
        }
        return null;
    }


}

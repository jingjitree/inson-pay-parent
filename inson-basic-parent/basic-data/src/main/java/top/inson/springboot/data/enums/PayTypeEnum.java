package top.inson.springboot.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayTypeEnum {
    WECHAT(1,"微信"),
    ALIPAY(2,"支付宝")

    ;

    public static PayTypeEnum getCategory(Integer code){
        if (code == null)
            return null;
        for (PayTypeEnum en : PayTypeEnum.values()) {
            if (code.equals(en.getCode()))
                return en;
        }
        return null;
    }

    private final int code;
    private final String desc;
}

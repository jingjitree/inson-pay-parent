package top.inson.springboot.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayTypeEnum {
    WECHAT(1,"微信"),
    ALIPAY(2,"支付宝")

    ;

    private final int code;
    private final String desc;
}

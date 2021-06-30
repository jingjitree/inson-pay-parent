package top.inson.springboot.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayCategoryEnum {
    UNIFIED_PAY(1,"主扫支付"),
    MICRO_PAY(2,"被扫支付"),
    JS_PAY(3,"公众号支付"),
    MINI_PAY(4,"小程序支付"),
    ALI_LIFE_PAY(5,"支付宝生活号支付"),
    APP_PAY(6,"app支付"),
    ;

    private final int code;
    private final String desc;
}

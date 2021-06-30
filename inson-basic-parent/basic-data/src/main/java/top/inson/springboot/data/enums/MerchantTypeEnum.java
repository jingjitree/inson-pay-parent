package top.inson.springboot.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MerchantTypeEnum {
    COMPANY(1,"企业"),
    BUSINESS(2,"个体户"),
    PERSON(3,"个人")
    ;

    private final int code;
    private final String desc;
}

package top.inson.springboot.pay.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.inson.springboot.common.enums.IBadBusiness;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PayBadBusinessEnum implements IBadBusiness {
    CHANNEL_NOT_SETTING(1001,"账户未配置渠道"),
    CHANNEL_NOT_EXISTS(1002,"所配置的渠道不存在"),


    MCH_ORDER_EXISTS(1101,"商户订单号不允许重复"),

    ;

    private final Integer code;
    private final String desc;
}

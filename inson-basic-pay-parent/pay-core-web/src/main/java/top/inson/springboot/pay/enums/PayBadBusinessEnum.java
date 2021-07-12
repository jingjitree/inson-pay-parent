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
    CREATE_ORDER_FAIL(1102,"下单失败"),
    PAY_MONEY_ERROR(1103,"支付金额有误"),


    BUSINESS_ERROR(2001,"支付业务出错"),
    BUSINESS_SIGN_ERROR(2002,"支付业务签名出错")
    ;

    private final Integer code;
    private final String desc;
}

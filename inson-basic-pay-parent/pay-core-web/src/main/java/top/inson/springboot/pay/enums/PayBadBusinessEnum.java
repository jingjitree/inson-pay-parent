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
    PAY_TYPE_ERROR(1104,"不支持的支付方式"),
    ORDER_NOT_EXISTS(1105,"订单不存在"),
    MUST_SEND_ORDER_NO(1106,"必须上送订单编号"),


    BUSINESS_ERROR(2001,"支付业务出错"),
    BUSINESS_SIGN_ERROR(2002,"支付业务签名出错"),
    SEND_REQUEST_ERROR(2003,"接口请求失败，请稍后再试"),
    ;

    private final Integer code;
    private final String desc;
}

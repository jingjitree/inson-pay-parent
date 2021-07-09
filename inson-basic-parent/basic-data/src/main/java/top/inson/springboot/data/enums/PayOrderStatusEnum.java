package top.inson.springboot.data.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PayOrderStatusEnum {
    PAYING(1,"支付中"),
    PAY_SUCCESS(2,"支付成功"),
    PAY_FAIL(3,"支付失败"),
    PAY_CANCEL(4,"已取消"),
    PARTIAL_REFUND(5,"部分退款"),
    FULL_REFUND(6,"全额退款"),
    ;

    private final int code;
    private final String desc;
}

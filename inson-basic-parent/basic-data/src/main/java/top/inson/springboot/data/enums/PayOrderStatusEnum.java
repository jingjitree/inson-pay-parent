package top.inson.springboot.data.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PayOrderStatusEnum {
    CREATE_ORDER(1,"创建成功"),
    PAYING(2,"支付中"),
    PAY_SUCCESS(3,"支付成功"),
    PAY_FAIL(4,"支付失败"),
    PAY_CANCEL(5,"已取消"),
    PARTIAL_REFUND(6,"部分退款"),
    FULL_REFUND(7,"全额退款"),
    CREATE_ORDER_FAIL(8,"下单失败"),
    ;

    private final int code;
    private final String desc;
}

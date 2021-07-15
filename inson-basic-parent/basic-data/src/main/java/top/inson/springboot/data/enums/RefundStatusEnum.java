package top.inson.springboot.data.enums;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum RefundStatusEnum {
    APPLY_REFUND(1,"申请退款"),
    REFUNDING(2,"退款中"),
    REFUND_SUCCESS(3,"退款成功"),
    REFUND_FAIL(4,"退款失败"),
    ;

    public static RefundStatusEnum getCategory(Integer code){
        if (code == null)
            return null;
        for (RefundStatusEnum en : RefundStatusEnum.values()) {
            if (code.equals(en.getCode()))
                return en;
        }
        return null;
    }

    private final int code;
    private final String desc;
}

package top.inson.springboot.pay.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.inson.springboot.common.enums.IBadBusiness;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PayBadBusinessEnum implements IBadBusiness {
    CHANNEL_NOT_SETTING(1001, "账户未配置渠道")

    ;

    private final Integer code;
    private final String desc;
}

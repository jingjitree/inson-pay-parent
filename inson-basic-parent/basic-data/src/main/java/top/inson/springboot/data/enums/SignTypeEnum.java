package top.inson.springboot.data.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SignTypeEnum {
    MD5(1,"MD5"),
    RSA(2,"RSA"),
    RSA2(3,"RSA2")
    ;


    private final int code;
    private final String desc;

}

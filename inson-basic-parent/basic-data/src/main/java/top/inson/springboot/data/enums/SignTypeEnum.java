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

    public static SignTypeEnum getCategory(Integer code){
        if (code == null)
            return null;
        for (SignTypeEnum en : SignTypeEnum.values()) {
            if (code.equals(en.getCode()))
                return en;
        }
        return null;
    }

    private final int code;
    private final String desc;

}

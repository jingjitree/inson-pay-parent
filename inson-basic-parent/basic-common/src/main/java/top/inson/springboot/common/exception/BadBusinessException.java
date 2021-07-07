package top.inson.springboot.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import top.inson.springboot.common.enums.IBadBusiness;

/**
 * 自定义业务异常
 */
@Getter
public class BadBusinessException extends RuntimeException{
    private Integer status = HttpStatus.METHOD_NOT_ALLOWED.value();

    public BadBusinessException(String msg){
        super(msg);
    }

    public BadBusinessException(IBadBusiness business){
        super(business.getDesc());
        this.status = business.getCode();
    }

}

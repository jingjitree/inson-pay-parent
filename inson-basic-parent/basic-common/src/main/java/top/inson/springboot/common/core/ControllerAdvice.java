package top.inson.springboot.common.core;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.inson.springboot.common.entity.response.CommonResult;


@Slf4j
@RestControllerAdvice
public class ControllerAdvice {


    @ExceptionHandler(value = Exception.class)
    public CommonResult handler(Exception e){
        log.info("异常信息", e);
        return CommonResult.fail(500, "系统异常");
    }

    @ExceptionHandler(value = NullPointerException.class)
    public CommonResult nullPointHandler(Exception e){
        log.info("捕捉到空异常", e);
        return CommonResult.fail(501, "系统空异常");
    }

    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public CommonResult missingServletRequestParamHandler(Exception e){
        log.info("缺少参数异常", e);
        return CommonResult.fail(502, "缺少必填参数");
    }

}

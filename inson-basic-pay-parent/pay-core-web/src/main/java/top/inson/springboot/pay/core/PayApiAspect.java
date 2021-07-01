package top.inson.springboot.pay.core;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Aspect
@Component
public class PayApiAspect {

    private Gson gson = new GsonBuilder().create();

    @Pointcut("@annotation(top.inson.springboot.pay.annotation.PayCheckSign)")
    public void pointCut(){
    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("环绕型通知：" );
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String signType = request.getHeader("signType");
        String paySign = request.getHeader("paySign");


        Object result = joinPoint.proceed();
        log.info("接口应答参数：{}", gson.toJson(result));
        return result;
    }

}

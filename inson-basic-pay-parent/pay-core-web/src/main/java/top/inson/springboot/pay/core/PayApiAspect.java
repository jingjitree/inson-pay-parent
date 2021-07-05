package top.inson.springboot.pay.core;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

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
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();

        //方法的所有参数
        Object[] params = joinPoint.getArgs();
        if (params.length == 0){
            return joinPoint.proceed();
        }

        //方法参数的注解
        Annotation[][] annotations = method.getParameterAnnotations();
        for (int i = 0, len = annotations.length; i < len; i++) {
            Object param = params[i];
            Annotation[] annotation = annotations[i];
            if (param == null || annotation.length == 0)
                continue;
            for (Annotation an : annotation) {
                if (an.annotationType().equals(RequestBody.class)){
                    String reqJson = gson.toJson(param);
                    log.info("{},方法的请求参数json:{}", method.getName(), reqJson);
                    this.validateSign(signType, paySign, reqJson);
                }
            }
        }
        Object result = joinPoint.proceed();
        log.info("接口应答参数：{}", gson.toJson(result));
        return result;
    }

    private void validateSign(String signType, String paySign, String reqJson) {
        log.info("signType:{}", signType);
        log.info("paySign:{}", paySign);
        //throw new BadRequestException("签名失败");
    }

}

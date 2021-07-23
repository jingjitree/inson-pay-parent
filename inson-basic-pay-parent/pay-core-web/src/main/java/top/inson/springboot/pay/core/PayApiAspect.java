package top.inson.springboot.pay.core;


import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.inson.springboot.common.exception.BadRequestException;
import top.inson.springboot.data.entity.MerCashier;
import top.inson.springboot.paycommon.service.IPayCacheService;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class PayApiAspect {


    @Autowired
    private IPayCacheService payCacheService;


    private final Gson gson = new GsonBuilder().create();

    @Pointcut("@annotation(top.inson.springboot.pay.annotation.PayCheckSign)")
    public void pointCut(){
    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("环绕型通知：" );
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
        JsonObject reqObj = gson.fromJson(reqJson, JsonObject.class);
        String cashier = reqObj.get("cashier").getAsString();
        if (StrUtil.isEmpty(signType))
            throw new BadRequestException("签名类型:signType，不能为空");
        if (StrUtil.isEmpty(paySign))
            throw new BadRequestException("签名:paySign,不能为空");
        if (StrUtil.isEmpty(cashier))
            throw new BadRequestException("商户账户：cashier，不能为空");

        //缓存中获取账号
        MerCashier merCashier = payCacheService.getCashier(cashier);
        if (merCashier == null)
            throw new BadRequestException("未查询到支付账户");

        log.info("签名类型signType:{}", signType);
        switch (signType){
            case "MD5":
                String signParams = reqJson + "&key=" + merCashier.getSignKey();
                log.info("签名串signParams：{}", signParams);
                String sign = DigestUtil.md5Hex(signParams).toUpperCase();
                if (!sign.equals(paySign)) {
                    log.info("paySign:{}, 服务器sign:{}", paySign, sign);
                    throw new BadRequestException("签名错误");
                }
                break;
            default:
                throw new BadRequestException("不支持的签名类型");
        }
    }

}

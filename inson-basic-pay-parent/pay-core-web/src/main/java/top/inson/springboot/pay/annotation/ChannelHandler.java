package top.inson.springboot.pay.annotation;

import org.springframework.stereotype.Service;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
//加上此注解，实现类将在spring中注册
@Service
public @interface ChannelHandler {
    String source();

}

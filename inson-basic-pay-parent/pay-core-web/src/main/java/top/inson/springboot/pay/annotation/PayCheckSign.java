package top.inson.springboot.pay.annotation;


import java.lang.annotation.*;


/**
 * @Target 注解的作用目标，METHOD表示作用在方法上
 * @Retention 注解的保留位置，RUNTIME表示注解可以存在于运行时，可用于反射
 * @Documented 说明改注解将包含在javadoc中
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PayCheckSign {
}

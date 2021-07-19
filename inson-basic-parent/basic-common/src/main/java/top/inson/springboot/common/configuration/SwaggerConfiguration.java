package top.inson.springboot.common.configuration;


import com.google.common.base.Predicates;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Slf4j
@Configuration
public class SwaggerConfiguration {

    @Bean
    public Docket docket(Environment environment){
        //设置要显示的swagger环境
        Profiles profiles = Profiles.of("dev", "test");
        //通过Environment.acceptsProfiles 判断是否处在自己设定的环境中
        boolean enable = environment.acceptsProfiles(profiles);
        log.info("是否开启swagger：{}", enable);

        return new Docket(DocumentationType.SWAGGER_2)
                //.groupName("开源boot")
                .enable(enable)
                .select()
                //配置要扫描接口的方式
                .apis(RequestHandlerSelectors.any())
                //过滤什么路径下的接口
                //.paths(PathSelectors.ant("/path/**"))
                //不显示错误的接口地址 basic-error-controller
                .paths(Predicates.not(PathSelectors.regex("/error.*")))
                .build();
    }


}

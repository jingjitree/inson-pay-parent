package top.inson.springboot;


import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableSwagger2Doc
//开启缓存
@EnableCaching
public class PayBoosApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayBoosApplication.class, args);
    }

}

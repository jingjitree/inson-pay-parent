package top.inson.springboot.pay;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
//扫包
@ComponentScan(basePackages = "top.inson.springboot")
public class PayCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayCoreApplication.class, args);
    }


}

package top.inson.springboot.notify;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "top.inson.springboot")
public class PayNotifyApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayNotifyApplication.class, args);
    }

}

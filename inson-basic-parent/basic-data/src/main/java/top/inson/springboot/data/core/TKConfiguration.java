package top.inson.springboot.data.core;

import org.springframework.context.annotation.Configuration;
import tk.mybatis.spring.annotation.MapperScan;

@Configuration
@MapperScan(basePackages = "top.inson.springboot.data.dao")
public class TKConfiguration {

}

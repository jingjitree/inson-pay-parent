package top.inson.springboot.security.constants;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ignore")
public class IgnoreUrlConstants {

    private List<String> urls;

}

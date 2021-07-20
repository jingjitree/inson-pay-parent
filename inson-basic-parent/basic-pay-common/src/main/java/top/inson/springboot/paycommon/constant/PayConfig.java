package top.inson.springboot.paycommon.constant;


import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "pay")
@PropertySource(value = "config/payConfig.properties")
public class PayConfig {

    @Value("${pay.baseUrl}")
    private String payBaseUrl;

    private String eNotifyUrl;

    private String eRefundNotifyUrl;


}

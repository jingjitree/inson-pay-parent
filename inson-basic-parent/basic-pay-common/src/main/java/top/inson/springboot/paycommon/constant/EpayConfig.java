package top.inson.springboot.paycommon.constant;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;


@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "epay")
@PropertySource(value = "config/epay/epayConfig.properties")
public class EpayConfig {

    private String certNo;

    private String certPwd;

    private String pubCertPath;
    private String certPath;
    private String signType;

    private String baseUrl;

    private String unifiedUrl;
    private String microPayUrl;
    private String orderQueryUrl;
    private String refundOrderUrl;
    private String refundQueryUrl;

}

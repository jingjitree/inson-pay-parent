package top.inson.springboot.security.constants;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
//必须
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConstants {

    private String header;

    private String secret;

    private Long expiration;

    private String onlineKey;

    private String codeKey;

    private Long detect;

    private Long renew;

}

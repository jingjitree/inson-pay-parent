package top.inson.springboot.notify.constant;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "trade.queue")
public class RabbitmqConstant {

    private String payDelayQueue;
    private String payDelayExchange;
    private String payDelayRoutingKey;

    private String payQueue;
    private String payExchange;
    private String payRoutingKey;



}

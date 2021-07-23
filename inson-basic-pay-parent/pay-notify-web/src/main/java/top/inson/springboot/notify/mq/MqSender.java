package top.inson.springboot.notify.mq;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Slf4j
@Component
public class MqSender {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息到消息队列
     * @param exchange
     * @param routingKey
     * @param message
     */
    public void send(String exchange, String routingKey, String message){
        log.info("发送到消息队列消息体：{}", message);
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData);
    }

    /**
     * 发送消息到延迟消息队列
     * @param delayExchange
     * @param delayRoutingKey
     * @param message
     * @param delayTime
     */
    public void send(String delayExchange, String delayRoutingKey, String message, long delayTime){
        log.info("发送消息体到延迟队列：message：{}，delayTime：{}", message, delayTime);
        rabbitTemplate.convertAndSend(delayExchange, delayRoutingKey, message, msg -> {
            msg.getMessageProperties().setExpiration(String.valueOf(delayTime));
            return msg;
        });
    }


}

package top.inson.springboot.notify.mq;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

@Slf4j
@Component
public class MqCustomer {


    @RabbitHandler
    @RabbitListener(queues = "${trade.queue.payQueue}")
    public void queueListener(Message message){
        String msg = new String(message.getBody(), Charset.defaultCharset());
        log.info("消息队列接收到消息体：{}", msg);
    }

    @RabbitHandler
    @RabbitListener(queues = "${trade.queue.payDelayQueue}")
    public void delayQueueListener(Message message){
        String msg = new String(message.getBody(), Charset.defaultCharset());
        log.info("延迟队列接收消息：{}", msg);

    }



}

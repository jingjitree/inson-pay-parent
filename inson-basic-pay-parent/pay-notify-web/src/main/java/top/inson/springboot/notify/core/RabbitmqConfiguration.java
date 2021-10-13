package top.inson.springboot.notify.core;


import cn.hutool.core.map.MapUtil;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.inson.springboot.notify.constant.RabbitmqConstant;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class RabbitmqConfiguration {
    @Autowired
    private RabbitmqConstant mqConstant;

    @Bean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory){
        return new RabbitTemplate(connectionFactory);
    }

    //三步骤：1.创建队列，2.创建交换机，3.绑定队列和交换机
    @Bean
    public Queue queue(){
        return new Queue(mqConstant.getPayQueue());
    }

    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(mqConstant.getPayExchange());
    }

    @Bean
    public Binding binding(){
        return BindingBuilder.bind(queue()).to(topicExchange()).with(mqConstant.getPayRoutingKey());
    }

    @Bean
    //延迟队列
    public Queue delayQueue(){
        Map<String, Object> params = MapUtil.builder(new HashMap<String, Object>())
                .put("x-dead-letter-exchange", mqConstant.getPayDelayExchange())
                .put("x-dead-letter-routing-key", mqConstant.getPayDelayRoutingKey())
                .build();
        return new Queue(mqConstant.getPayDelayQueue(), true, false, false, params);
    }
    @Bean
    public Queue refundDelayQueue(){
        Map<String, Object> params = MapUtil.builder(new HashMap<String, Object>())
                .put("x-dead-letter-exchange", mqConstant.getPayDelayExchange())
                .put("x-dead-letter-routing-key", mqConstant.getRefundDelayRoutingKey())
                .build();
        return new Queue(mqConstant.getRefundDelayQueue(), true, false, false, params);
    }

    @Bean
    public DirectExchange delayExchange(){
        DirectExchange directExchange = new DirectExchange(mqConstant.getPayDelayExchange());
        directExchange.setDelayed(true);
        return directExchange;
    }

    @Bean
    public Binding delayBinding(){
        return BindingBuilder.bind(delayQueue()).to(delayExchange()).with(mqConstant.getPayDelayRoutingKey());
    }
    @Bean
    public Binding refundDelayBinding(){
        return BindingBuilder.bind(refundDelayQueue()).to(delayExchange()).with(mqConstant.getRefundDelayRoutingKey());
    }

}

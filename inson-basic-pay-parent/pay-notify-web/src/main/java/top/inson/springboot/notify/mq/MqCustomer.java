package top.inson.springboot.notify.mq;


import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.inson.springboot.notify.constant.RabbitmqConstant;
import top.inson.springboot.paycommon.constant.PayMqConstant;
import top.inson.springboot.utils.HttpUtils;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Map;

@Slf4j
@Component
public class MqCustomer {
    @Autowired
    private RabbitmqConstant mqConstant;
    @Autowired
    private MqSender mqSender;


    private final Gson gson = new GsonBuilder().create();
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
        this.sendNotify(msg, mqConstant.getPayDelayRoutingKey());
    }

    @RabbitListener(queues = "${trade.queue.refundDelayQueue}")
    public void refundQueueListener(Message message){
        String msg = new String(message.getBody(), Charset.defaultCharset());
        log.info("退款延迟队列接收到msg：{}", msg);
        this.sendNotify(msg, mqConstant.getRefundDelayRoutingKey());
    }

    private void sendNotify(String msg, String delayRoutingKey) {
        if (StrUtil.isBlank(msg))
            return;
        JsonObject msgObj = gson.fromJson(msg, JsonObject.class);
        int notifyCount = msgObj.get(PayMqConstant.NOTIFY_COUNT).getAsInt();
        if (notifyCount > 5) {
            log.info("已超过最大通知次数" + notifyCount);
            return;
        }
        int delayTime = ++notifyCount * 5 * 1000;
        log.info("队列delayTime：" + delayTime);
        String notifyUrl = msgObj.get(PayMqConstant.NOTIFY_URL).getAsString();
        JsonObject jsonDataObj = msgObj.get(PayMqConstant.NOTIFY_DATA).getAsJsonObject();
        log.info("队列jsonData:{}", jsonDataObj);
        //发送请求
        HttpResponse response = null;
        try {
            JsonObject headerObj = msgObj.get(PayMqConstant.NOTIFY_HEADERS).getAsJsonObject();
            log.info("队列headerObj: {}", headerObj);
            Type type = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> headers = gson.fromJson(headerObj.toString(), type);
            response = HttpUtils.sendPostJson(notifyUrl, headers, jsonDataObj.toString());
        } catch (Exception e) {
            log.error("回调通知异常", e);
        }
        if (response != null && response.isOk()) {
            String body = response.body();
            log.info("下游通知结果：" + body);
            if ("SUCCESS".equalsIgnoreCase(body)) {
                return;
            }
        }
        msgObj.remove(PayMqConstant.NOTIFY_COUNT);
        msgObj.addProperty(PayMqConstant.NOTIFY_COUNT, notifyCount);
        mqSender.send(mqConstant.getPayDelayExchange(), delayRoutingKey, msgObj.toString(), delayTime);
    }



}

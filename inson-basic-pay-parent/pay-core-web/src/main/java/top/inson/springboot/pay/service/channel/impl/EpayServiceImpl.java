package top.inson.springboot.pay.service.channel.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import top.inson.springboot.data.entity.PayOrder;
import top.inson.springboot.pay.annotation.ChannelHandler;
import top.inson.springboot.pay.constant.EpayConfig;
import top.inson.springboot.pay.service.channel.IChannelService;


@Slf4j
@ChannelHandler(source = "YPL")
public class EpayServiceImpl implements IChannelService {
    @Autowired
    private EpayConfig epayConfig;



    @Override
    public void unifiedOrder(PayOrder payOrder) {
        log.info("易票联渠道主扫处理的逻辑");
        String reqUrl = epayConfig.getBaseUrl() + epayConfig.getUnifiedUrl();
        log.info("主扫下单地址：" + reqUrl);
    }

}

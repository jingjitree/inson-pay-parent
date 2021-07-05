package top.inson.springboot.pay.service.channel.impl;

import lombok.extern.slf4j.Slf4j;
import top.inson.springboot.data.entity.PayOrder;
import top.inson.springboot.pay.annotation.ChannelHandler;
import top.inson.springboot.pay.service.channel.IChannelService;


@Slf4j
@ChannelHandler(source = "YPL")
public class EpayServiceImpl implements IChannelService {

    @Override
    public void unifiedOrder(PayOrder payOrder) {
        log.info("易票联渠道主扫处理的逻辑");


    }

}

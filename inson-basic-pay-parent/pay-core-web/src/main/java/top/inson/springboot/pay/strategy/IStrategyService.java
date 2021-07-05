package top.inson.springboot.pay.strategy;

import top.inson.springboot.pay.service.channel.IChannelService;

public interface IStrategyService {

    IChannelService getChannelService(String source);

}

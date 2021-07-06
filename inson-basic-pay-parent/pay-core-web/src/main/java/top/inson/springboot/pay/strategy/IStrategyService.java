package top.inson.springboot.pay.strategy;

import top.inson.springboot.pay.service.channel.IChannelService;

/**
 * 策略模式的service
 */
public interface IStrategyService {

    IChannelService getChannelService(String source);

}

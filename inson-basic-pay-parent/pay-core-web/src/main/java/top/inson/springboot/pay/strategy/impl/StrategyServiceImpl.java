package top.inson.springboot.pay.strategy.impl;

import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;
import top.inson.springboot.pay.annotation.ChannelHandler;
import top.inson.springboot.pay.service.channel.IChannelService;
import top.inson.springboot.pay.strategy.IStrategyService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StrategyServiceImpl implements IStrategyService {
    private Map<String, IChannelService> channelsMap;

    @Autowired
    private void setChannelsMap(List<IChannelService> channelServices){
        channelsMap = channelServices.stream().collect(
                Collectors.toMap(channel -> AnnotationUtils.findAnnotation(channel.getClass(), ChannelHandler.class).source(),
                        v -> v, (v1, v2) -> v1)
        );
    }

    @Override
    public IChannelService getChannelService(String source) {
        if (StrUtil.isEmpty(source))
            return null;
        return channelsMap.get(source);
    }

}

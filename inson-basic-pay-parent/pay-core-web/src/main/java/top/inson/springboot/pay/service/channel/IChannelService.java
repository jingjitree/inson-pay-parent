package top.inson.springboot.pay.service.channel;

import top.inson.springboot.data.entity.ChannelSubmerConfig;
import top.inson.springboot.data.entity.PayOrder;
import top.inson.springboot.pay.entity.dto.MicroPayDto;
import top.inson.springboot.pay.entity.dto.UnifiedOrderDto;

public interface IChannelService {

    UnifiedOrderDto unifiedOrder(PayOrder payOrder, ChannelSubmerConfig submerConfig);

    MicroPayDto microPay(PayOrder payOrder, ChannelSubmerConfig submerConfig);

    void refundOrder();

    void orderQuery();

    void refundQuery();

}

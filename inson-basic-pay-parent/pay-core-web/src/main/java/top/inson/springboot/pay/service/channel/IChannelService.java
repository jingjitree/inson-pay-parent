package top.inson.springboot.pay.service.channel;

import top.inson.springboot.data.entity.ChannelSubmerConfig;
import top.inson.springboot.data.entity.PayOrder;
import top.inson.springboot.data.entity.RefundOrder;
import top.inson.springboot.paycommon.entity.dto.*;

public interface IChannelService {

    UnifiedOrderDto unifiedOrder(PayOrder payOrder, ChannelSubmerConfig submerConfig);

    MicroPayDto microPay(PayOrder payOrder, ChannelSubmerConfig submerConfig);

    RefundOrderDto refundOrder(RefundOrder refundOrder, ChannelSubmerConfig submerConfig);

    OrderQueryDto orderQuery(PayOrder payOrder, ChannelSubmerConfig submerConfig);

    RefundQueryDto refundQuery(RefundOrder refundOrder, ChannelSubmerConfig submerConfig);

}

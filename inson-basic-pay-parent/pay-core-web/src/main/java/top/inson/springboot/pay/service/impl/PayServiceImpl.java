package top.inson.springboot.pay.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import top.inson.springboot.common.exception.BadBusinessException;
import top.inson.springboot.data.dao.IMerCashierMapper;
import top.inson.springboot.data.dao.IMerChannelSettingMapper;
import top.inson.springboot.data.dao.IPayOrderMapper;
import top.inson.springboot.data.entity.MerCashier;
import top.inson.springboot.data.entity.MerChannelSetting;
import top.inson.springboot.data.entity.PayOrder;
import top.inson.springboot.pay.entity.dto.UnifiedOrderDto;
import top.inson.springboot.pay.entity.vo.UnifiedOrderVo;
import top.inson.springboot.pay.enums.PayBadBusinessEnum;
import top.inson.springboot.pay.service.IPayCacheService;
import top.inson.springboot.pay.service.IPayService;
import top.inson.springboot.pay.service.channel.IChannelService;
import top.inson.springboot.pay.strategy.IStrategyService;


@Slf4j
@Service
public class PayServiceImpl implements IPayService {
    @Autowired
    private IMerCashierMapper merCashierMapper;
    @Autowired
    private IMerChannelSettingMapper merChannelSettingMapper;
    @Autowired
    private IPayOrderMapper payOrderMapper;


    @Autowired
    private IStrategyService strategyService;

    @Autowired
    private IPayCacheService payCacheService;


    private final Gson gson = new GsonBuilder().create();
    @Override
    public UnifiedOrderDto unifiedOrder(UnifiedOrderVo vo) throws Exception{
        MerCashier merCashier = payCacheService.getCashier(vo.getCashier());
        log.info("支付账户cashier、{}", gson.toJson(merCashier));
        PayOrder payOrder = new PayOrder();
        IChannelService channelService = this.validPayParam(vo, merCashier.getMerchantNo(), payOrder);
        //保存支付订单
        this.savePayOrder(vo, merCashier, payOrder);

        channelService.unifiedOrder(payOrder);
        return null;
    }

    private IChannelService validPayParam(UnifiedOrderVo vo, String merchantNo, PayOrder payOrder) {
        Example orderExample = new Example(PayOrder.class);
        orderExample.createCriteria()
                .andEqualTo("mchOrderNo", vo.getMchOrderNo());
        int countOrder = payOrderMapper.selectCountByExample(orderExample);
        if (countOrder > 0)
            throw new BadBusinessException(PayBadBusinessEnum.MCH_ORDER_EXISTS);

        Example example = new Example(MerChannelSetting.class);
        example.createCriteria()
                .andEqualTo("merchantNo", merchantNo)
                .andEqualTo("enable", Boolean.TRUE)
                .andEqualTo("payType", vo.getPayType());
        MerChannelSetting merChannel = merChannelSettingMapper.selectOneByExample(example);
        if (merChannel == null)
            throw new BadBusinessException(PayBadBusinessEnum.CHANNEL_NOT_SETTING);
        //渠道编号
        String channelNo = merChannel.getChannelNo();
        IChannelService channelService = strategyService.getChannelService(channelNo);
        if (channelService == null)
            throw new BadBusinessException(PayBadBusinessEnum.CHANNEL_NOT_EXISTS);
        //构建订单信息
        payOrder.setChannelNo(channelNo)
                .setCashier(vo.getCashier())
                .setPayType(vo.getPayType());
        return channelService;
    }

    private void savePayOrder(UnifiedOrderVo vo, MerCashier merCashier, PayOrder payOrder) {
        //平台订单号
        String orderNo = DateUtil.format(DateUtil.date(), DatePattern.PURE_DATETIME_PATTERN) +
                merCashier.getMerchantNo() + RandomUtil.randomNumbers(8);
        log.info("支付订单号orderNo:" + orderNo);
        payOrder.setOrderNo(orderNo)
                .setMchOrderNo(vo.getMchOrderNo())
                .setMerchantNo(merCashier.getMerchantNo());


    }

}

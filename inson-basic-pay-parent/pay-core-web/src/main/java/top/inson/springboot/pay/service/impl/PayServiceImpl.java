package top.inson.springboot.pay.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import top.inson.springboot.common.exception.BadBusinessException;
import top.inson.springboot.data.dao.IChannelSubmerConfigMapper;
import top.inson.springboot.data.dao.IMerCashierMapper;
import top.inson.springboot.data.dao.IMerChannelSettingMapper;
import top.inson.springboot.data.dao.IPayOrderMapper;
import top.inson.springboot.data.entity.ChannelSubmerConfig;
import top.inson.springboot.data.entity.MerCashier;
import top.inson.springboot.data.entity.MerChannelSetting;
import top.inson.springboot.data.entity.PayOrder;
import top.inson.springboot.data.enums.PayCategoryEnum;
import top.inson.springboot.data.enums.PayOrderStatusEnum;
import top.inson.springboot.data.enums.PayTypeEnum;
import top.inson.springboot.pay.entity.dto.MicroPayDto;
import top.inson.springboot.pay.entity.dto.UnifiedOrderDto;
import top.inson.springboot.pay.entity.vo.MicroPayVo;
import top.inson.springboot.pay.entity.vo.UnifiedOrderVo;
import top.inson.springboot.pay.enums.PayBadBusinessEnum;
import top.inson.springboot.pay.service.IPayCacheService;
import top.inson.springboot.pay.service.IPayService;
import top.inson.springboot.pay.service.channel.IChannelService;
import top.inson.springboot.pay.strategy.IStrategyService;
import top.inson.springboot.utils.AmountUtil;
import top.inson.springboot.utils.PayUtils;

import java.math.BigDecimal;


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
    private IChannelSubmerConfigMapper channelSubmerConfigMapper;


    @Autowired
    private IStrategyService strategyService;

    @Autowired
    private IPayCacheService payCacheService;


    private final Gson gson = new GsonBuilder().create();
    @Override
    public UnifiedOrderDto unifiedOrder(UnifiedOrderVo vo) throws Exception{
        MerCashier merCashier = payCacheService.getCashier(vo.getCashier());
        log.info("支付账户cashier、{}", gson.toJson(merCashier));
        String merchantNo = merCashier.getMerchantNo();
        //构建支付订单
        PayOrder payOrder = new PayOrder()
                .setPayCategory(PayCategoryEnum.UNIFIED_PAY.getCode());
        IChannelService channelService = this.validPayParam(vo.getMchOrderNo(), vo.getPayMoney(), vo.getPayType(),
                merchantNo, payOrder);
        //保存支付订单
        BeanUtil.copyProperties(vo, payOrder);
        this.savePayOrder(merchantNo, payOrder);

        //查询渠道配置
        Example subCofExample = new Example(ChannelSubmerConfig.class);
        subCofExample.createCriteria()
                .andEqualTo("channelNo", payOrder.getChannelNo())
                .andEqualTo("merchantNo", payOrder.getMerchantNo())
                .andEqualTo("payType", payOrder.getPayType());
        ChannelSubmerConfig submerConfig = channelSubmerConfigMapper.selectOneByExample(subCofExample);

        UnifiedOrderDto unifiedDto = channelService.unifiedOrder(payOrder, submerConfig);
        if (unifiedDto != null){
            Example example = new Example(PayOrder.class);
            example.createCriteria()
                    .andEqualTo("orderNo", payOrder.getOrderNo());
            //请求成功，更新订单状态
            PayOrder upOrder = new PayOrder()
                    .setOrderStatus(PayOrderStatusEnum.PAYING.getCode())
                    .setOrderDesc(StrUtil.isBlank(unifiedDto.getOrderDesc()) ? "下单成功" : unifiedDto.getOrderDesc());
            payOrderMapper.updateByExampleSelective(upOrder, example);
            //设置接口返回参数
            payOrder.setOrderStatus(upOrder.getOrderStatus())
                    .setOrderDesc(upOrder.getOrderDesc());
            BeanUtil.copyProperties(payOrder, unifiedDto);
        }
        return unifiedDto;
    }


    @Override
    public MicroPayDto microPay(MicroPayVo vo) {
        MerCashier merCashier = payCacheService.getCashier(vo.getCashier());
        String merchantNo = merCashier.getMerchantNo();

        String clientType = PayUtils.getClientTypeByCode(vo.getAuthCode());
        vo.setPayType(Integer.parseInt(clientType));

        //构建支付订单
        PayOrder payOrder = new PayOrder()
                .setPayCategory(PayCategoryEnum.MICRO_PAY.getCode());
        IChannelService channelService = this.validPayParam(vo.getMchOrderNo(), vo.getPayMoney(), vo.getPayType(),
                merchantNo, payOrder);
        //保存支付订单
        BeanUtil.copyProperties(vo, payOrder);
        this.savePayOrder(merchantNo, payOrder);

        //查询渠道配置
        Example subCofExample = new Example(ChannelSubmerConfig.class);
        subCofExample.createCriteria()
                .andEqualTo("channelNo", payOrder.getChannelNo())
                .andEqualTo("merchantNo", payOrder.getMerchantNo())
                .andEqualTo("payType", payOrder.getPayType());
        ChannelSubmerConfig submerConfig = channelSubmerConfigMapper.selectOneByExample(subCofExample);

        MicroPayDto payDto = channelService.microPay(payOrder, submerConfig);
        if (payDto != null) {
            Example example = new Example(PayOrder.class);
            example.createCriteria()
                    .andEqualTo("orderNo", payOrder.getOrderNo());
            //请求成功，更新订单状态
            PayOrder upOrder = new PayOrder()
                    .setOrderStatus(payDto.getOrderStatus())
                    .setOrderDesc(StrUtil.isBlank(payDto.getOrderDesc()) ? "下单成功" : payDto.getOrderDesc());
            payOrderMapper.updateByExampleSelective(upOrder, example);
            //设置接口返回参数
            payOrder.setOrderStatus(upOrder.getOrderStatus())
                    .setOrderDesc(upOrder.getOrderDesc());
            BeanUtil.copyProperties(payOrder, payDto);
        }
        return payDto;
    }

    private IChannelService validPayParam(String mchOrderNo, Integer payMoney, Integer payType, String merchantNo, PayOrder payOrder) {
        BigDecimal bigAmount = new BigDecimal(AmountUtil.changeFenToYuan(payMoney));
        if (BigDecimal.ZERO.compareTo(bigAmount) > 0) {
            throw new BadBusinessException(PayBadBusinessEnum.PAY_MONEY_ERROR);
        }
        if (PayTypeEnum.getCategory(payType) == null)
            throw new BadBusinessException(PayBadBusinessEnum.PAY_TYPE_ERROR);

        Example orderExample = new Example(PayOrder.class);
        orderExample.createCriteria()
                .andEqualTo("mchOrderNo", mchOrderNo);
        int countOrder = payOrderMapper.selectCountByExample(orderExample);
        if (countOrder > 0)
            throw new BadBusinessException(PayBadBusinessEnum.MCH_ORDER_EXISTS);

        Example example = new Example(MerChannelSetting.class);
        example.createCriteria()
                .andEqualTo("merchantNo", merchantNo)
                .andEqualTo("enable", Boolean.TRUE)
                .andEqualTo("payType", payType);
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
                .setPayAmount(bigAmount);
        return channelService;
    }

    private void savePayOrder(String merchantNo, PayOrder payOrder) {
        //平台订单号
        String orderNo = DateUtil.format(DateUtil.date(), DatePattern.PURE_DATETIME_PATTERN) +
                merchantNo.substring(8) + RandomUtil.randomNumbers(8);
        log.info("支付订单号orderNo:" + orderNo);
        payOrder.setOrderNo(orderNo)
                .setMerchantNo(merchantNo)
                .setOrderStatus(PayOrderStatusEnum.CREATE_ORDER.getCode());
        payOrderMapper.insertSelective(payOrder);

    }

}

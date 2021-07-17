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
import top.inson.springboot.data.dao.*;
import top.inson.springboot.data.entity.*;
import top.inson.springboot.data.enums.PayCategoryEnum;
import top.inson.springboot.data.enums.PayOrderStatusEnum;
import top.inson.springboot.data.enums.PayTypeEnum;
import top.inson.springboot.data.enums.RefundStatusEnum;
import top.inson.springboot.pay.entity.dto.*;
import top.inson.springboot.pay.entity.vo.*;
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
    private IRefundOrderMapper refundOrderMapper;


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
            PayOrder newOrder = this.upPayOrder(unifiedDto, payOrder.getOrderNo());
            BeanUtil.copyProperties(newOrder, unifiedDto);
            unifiedDto.setPayAmount(AmountUtil.changeYuanToFen(newOrder.getPayAmount()));
        }
        return unifiedDto;
    }


    @Override
    public MicroPayDto microPay(MicroPayVo vo) {
        MerCashier merCashier = payCacheService.getCashier(vo.getCashier());
        String merchantNo = merCashier.getMerchantNo();

        String clientType = PayUtils.getClientTypeByCode(vo.getAuthCode());
        if (StrUtil.isBlank(clientType))
            throw new BadBusinessException(PayBadBusinessEnum.AUTH_CODE_ERROR);
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
            PayOrder newOrder = this.upPayOrder(payDto, payOrder.getOrderNo());
            BeanUtil.copyProperties(newOrder, payDto);
            payDto.setPayAmount(AmountUtil.changeYuanToFen(newOrder.getPayAmount()));
        }
        return payDto;
    }

    @Override
    public RefundOrderDto refundOrder(RefundOrderVo vo) {
        PayOrder payOrder = this.validPayOrder(vo.getCashier(), vo.getOrderNo(), null);

        RefundOrder refundOrder = new RefundOrder();
        IChannelService channelService = this.validRefundParam(vo, payOrder, refundOrder);

        BeanUtil.copyProperties(vo, refundOrder);
        this.saveRefundOrder(refundOrder);
        //查询渠道配置
        Example subCofExample = new Example(ChannelSubmerConfig.class);
        subCofExample.createCriteria()
                .andEqualTo("channelNo", payOrder.getChannelNo())
                .andEqualTo("merchantNo", payOrder.getMerchantNo())
                .andEqualTo("payType", payOrder.getPayType());
        ChannelSubmerConfig submerConfig = channelSubmerConfigMapper.selectOneByExample(subCofExample);

        RefundOrderDto refundDto = channelService.refundOrder(refundOrder, submerConfig);
        if (refundDto != null){
            RefundOrder newOrder = this.upRefundOrder(refundDto, refundOrder.getRefundNo());
            RefundStatusEnum category = RefundStatusEnum.getCategory(newOrder.getRefundStatus());
            if (RefundStatusEnum.REFUNDING == category
                    || RefundStatusEnum.REFUND_SUCCESS == category){
                //退款申请成功
                PayOrder upOrder = new PayOrder()
                        .setAllRefundAmount(payOrder.getAllRefundAmount());
                Example example = new Example(PayOrder.class);
                example.createCriteria()
                        .andEqualTo("orderNo", payOrder.getOrderNo());
                payOrderMapper.updateByExampleSelective(upOrder, example);
            }
            BeanUtil.copyProperties(newOrder, refundDto);
            refundDto.setRefundMoney(AmountUtil.changeYuanToFen(newOrder.getRefundAmount()));
        }
        return refundDto;
    }

    private void saveRefundOrder(RefundOrder refundOrder) {
        String refundNo = DateUtil.format(DateUtil.date(), DatePattern.PURE_DATETIME_PATTERN) + RandomUtil.randomNumbers(8);
        log.info("退款订单号refundNo:" + refundNo);
        refundOrder.setRefundNo(refundNo);
        refundOrderMapper.insertSelective(refundOrder);
    }

    private IChannelService validRefundParam(RefundOrderVo vo, PayOrder payOrder, RefundOrder refundOrder) {
        PayOrderStatusEnum statusEnum = PayOrderStatusEnum.getCategory(payOrder.getOrderStatus());
        if (statusEnum != PayOrderStatusEnum.PAY_SUCCESS
                && statusEnum != PayOrderStatusEnum.PARTIAL_REFUND)
            throw new BadBusinessException(PayBadBusinessEnum.ORDER_NOT_ALLOW_REFUND);
        Example example = new Example(RefundOrder.class);
        example.createCriteria()
                .andEqualTo("mchRefundNo", vo.getMchRefundNo());
        int count = refundOrderMapper.selectCountByExample(example);
        if (count > 0)
            throw new BadBusinessException(PayBadBusinessEnum.REFUND_ORDER_EXISTS);

        BigDecimal bigRefundMoney = new BigDecimal(AmountUtil.changeFenToYuan(vo.getRefundMoney()));
        BigDecimal payAmount = payOrder.getPayAmount();
        BigDecimal allRefundAmount = payOrder.getAllRefundAmount();
        if (allRefundAmount == null)
            allRefundAmount = BigDecimal.ZERO;
        //剩余金额=支付金额-总退款金额-本次退款金额
        BigDecimal balanceAmount = payAmount.subtract(allRefundAmount).subtract(bigRefundMoney);
        log.info("余额={},支付金额{},-总退款金额{},-本次退款金额{}", balanceAmount, payAmount, allRefundAmount, bigRefundMoney);
        if (balanceAmount.compareTo(BigDecimal.ZERO) < 0)
            throw new BadBusinessException(PayBadBusinessEnum.REFUND_MONEY_ERROR);
        BigDecimal newAllRefundMoney = allRefundAmount.add(bigRefundMoney);
        log.info("总退款金额={},总退款金额{},+本次退款金额{}", newAllRefundMoney, allRefundAmount, bigRefundMoney);

        //构建退款订单参数
        payOrder.setAllRefundAmount(newAllRefundMoney);

        refundOrder.setRefundAmount(bigRefundMoney)
                .setPayOrderNo(payOrder.getOrderNo())
                .setMerchantNo(payOrder.getMerchantNo())
                .setRefundStatus(RefundStatusEnum.APPLY_REFUND.getCode())
                .setChannelNo(payOrder.getChannelNo());

        //根据订单标识获取渠道
        IChannelService channelService = strategyService.getChannelService(payOrder.getChannelNo());
        if (channelService == null)
            throw new BadBusinessException(PayBadBusinessEnum.CHANNEL_NOT_EXISTS);
        return channelService;
    }

    @Override
    public OrderQueryDto orderQuery(OrderQueryVo vo) {
        PayOrder payOrder = this.validPayOrder(vo.getCashier(), vo.getOrderNo(), vo.getMchOrderNo());
        log.info("订单查询orderNo：" + payOrder.getOrderNo());
        //根据订单标识获取渠道
        IChannelService channelService = strategyService.getChannelService(payOrder.getChannelNo());
        if (channelService == null)
            throw new BadBusinessException(PayBadBusinessEnum.CHANNEL_NOT_EXISTS);

        //查询渠道配置
        Example subCofExample = new Example(ChannelSubmerConfig.class);
        subCofExample.createCriteria()
                .andEqualTo("channelNo", payOrder.getChannelNo())
                .andEqualTo("merchantNo", payOrder.getMerchantNo())
                .andEqualTo("payType", payOrder.getPayType());
        ChannelSubmerConfig submerConfig = channelSubmerConfigMapper.selectOneByExample(subCofExample);

        OrderQueryDto queryDto = channelService.orderQuery(payOrder, submerConfig);
        if (queryDto != null){
            PayOrder newOrder = this.upPayOrder(queryDto, payOrder.getOrderNo());
            //设置接口返回参数
            BeanUtil.copyProperties(newOrder, queryDto);
            queryDto.setPayAmount(AmountUtil.changeYuanToFen(newOrder.getPayAmount()));
        }
        return queryDto;
    }

    private PayOrder validPayOrder(String cashier, String orderNo, String mchOrderNo) {
        Example orderExample = new Example(PayOrder.class);
        Example.Criteria criteria = orderExample.createCriteria()
                .andEqualTo("cashier", cashier);
        if (StrUtil.isNotBlank(orderNo))
            criteria.andEqualTo("orderNo", orderNo);
        else if (StrUtil.isNotBlank(mchOrderNo))
            criteria.andEqualTo("mchOrderNo", mchOrderNo);
        else
            throw new BadBusinessException(PayBadBusinessEnum.MUST_SEND_ORDER_NO);
        PayOrder payOrder = payOrderMapper.selectOneByExample(orderExample);
        if (payOrder == null)
            throw new BadBusinessException(PayBadBusinessEnum.ORDER_NOT_EXISTS);
        return payOrder;
    }

    @Override
    public RefundQueryDto refundQuery(RefundQueryVo vo) {
        PayOrder payOrder = new PayOrder();
        RefundOrder refundOrder = new RefundOrder();
        IChannelService channelService = this.validRefundQueryParam(vo, payOrder, refundOrder);

        //查询渠道配置
        Example subCofExample = new Example(ChannelSubmerConfig.class);
        subCofExample.createCriteria()
                .andEqualTo("channelNo", refundOrder.getChannelNo())
                .andEqualTo("merchantNo", refundOrder.getMerchantNo())
                .andEqualTo("payType", payOrder.getPayType());
        ChannelSubmerConfig submerConfig = channelSubmerConfigMapper.selectOneByExample(subCofExample);

        RefundQueryDto queryDto = channelService.refundQuery(refundOrder, submerConfig);
        if (queryDto != null){
            RefundOrder newRefund = this.upRefundOrder(queryDto, refundOrder.getRefundNo());
            BeanUtil.copyProperties(newRefund, queryDto);
            queryDto.setRefundMoney(AmountUtil.changeYuanToFen(newRefund.getRefundAmount()));
        }
        return queryDto;
    }

    private IChannelService validRefundQueryParam(RefundQueryVo vo, PayOrder payOrder, RefundOrder refundOrder) {
        RefundOrder refundData = refundOrderMapper.selectOne(refundOrder
                .setRefundNo(vo.getRefundNo())
                .setCashier(vo.getCashier())
        );
        if (refundData == null)
            throw new BadBusinessException(PayBadBusinessEnum.REFUND_ORDER_NOT_EXISTS);
        BeanUtil.copyProperties(refundData, refundOrder);

        PayOrder payData = payOrderMapper.selectOne(payOrder
                .setOrderNo(refundOrder.getPayOrderNo())
        );
        BeanUtil.copyProperties(payData, payOrder);

        //根据订单标识获取渠道
        IChannelService channelService = strategyService.getChannelService(refundOrder.getChannelNo());
        if (channelService == null)
            throw new BadBusinessException(PayBadBusinessEnum.CHANNEL_NOT_EXISTS);
        return channelService;
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

    private PayOrder upPayOrder(PayBaseDto baseDto, String orderNo) {
        Example example = new Example(PayOrder.class);
        example.createCriteria()
                .andEqualTo("orderNo", orderNo);
        //请求成功，更新订单状态
        PayOrder upOrder = new PayOrder();
        BeanUtil.copyProperties(baseDto, upOrder);
        if (StrUtil.isNotBlank(upOrder.getOrderNo()))
            upOrder.setOrderNo(null);
        log.info("更新订单参数upOrder：{}", gson.toJson(upOrder));
        payOrderMapper.updateByExampleSelective(upOrder, example);
        return payOrderMapper.selectOneByExample(example);
    }

    private RefundOrder upRefundOrder(RefundBaseDto baseDto, String refundNo) {
        Example example = new Example(RefundOrder.class);
        example.createCriteria()
                .andEqualTo("refundNo", refundNo);
        RefundOrder upOrder = new RefundOrder();
        BeanUtil.copyProperties(baseDto, upOrder);
        if (StrUtil.isNotBlank(upOrder.getRefundNo()))
            upOrder.setRefundNo(null);

        log.info("退款更新参数：{}", gson.toJson(upOrder));
        refundOrderMapper.updateByExampleSelective(upOrder, example);
        return refundOrderMapper.selectOneByExample(example);
    }
}

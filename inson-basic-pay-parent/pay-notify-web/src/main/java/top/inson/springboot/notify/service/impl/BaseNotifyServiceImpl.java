package top.inson.springboot.notify.service.impl;

import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import top.inson.springboot.data.dao.IPayOrderMapper;
import top.inson.springboot.data.entity.PayOrder;
import top.inson.springboot.data.enums.PayOrderStatusEnum;
import top.inson.springboot.notify.service.IBaseNotifyService;


@Slf4j
@Service
public class BaseNotifyServiceImpl implements IBaseNotifyService {
    @Autowired
    private IPayOrderMapper payOrderMapper;


    private final Gson gson = new GsonBuilder().create();
    @Override
    public void payOrderNotify(PayOrder upOrder, String orderNo) {
        Example example = new Example(PayOrder.class);
        example.createCriteria()
                .andEqualTo("orderNo", orderNo);

        PayOrder payOrder = payOrderMapper.selectOneByExample(example);
        if (payOrder == null){
            log.info("找不到改订单orderNo：" + orderNo);
            return;
        }
        PayOrderStatusEnum category = PayOrderStatusEnum.getCategory(payOrder.getOrderStatus());
        //部分退款或全额退款的订单，支付接口不更新状态
        if (category != PayOrderStatusEnum.PARTIAL_REFUND
                && category != PayOrderStatusEnum.FULL_REFUND) {
            //更新支付订单
            if (StrUtil.isNotBlank(upOrder.getOrderNo()))
                upOrder.setOrderNo(null);
            log.info("更新订单参数upOrder: {}", gson.toJson(upOrder));
            payOrderMapper.updateByExampleSelective(upOrder, example);
        }
        //通知下游商户

    }
}

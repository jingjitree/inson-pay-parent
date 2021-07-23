package top.inson.springboot.paycommon.service.impl;

import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import top.inson.springboot.data.dao.IMerCashierMapper;
import top.inson.springboot.data.entity.MerCashier;
import top.inson.springboot.paycommon.constant.PayRedisConstant;
import top.inson.springboot.paycommon.service.IPayCacheService;
import top.inson.springboot.utils.RedisUtils;

import java.util.concurrent.TimeUnit;


@Service
public class PayCacheServiceImpl implements IPayCacheService {
    @Autowired
    private IMerCashierMapper merCashierMapper;


    @Autowired
    private RedisUtils redisUtils;

    @Override
    public MerCashier getCashier(String cashier) {
        if (StrUtil.isEmpty(cashier))
            return null;
        //将账户存入redis中
        if (!redisUtils.hasHashKey(PayRedisConstant.PAY_CASHIER_PREFIX + cashier, PayRedisConstant.CASHIER_KEY)){
            Example example = new Example(MerCashier.class);
            example.createCriteria()
                    .andEqualTo("cashier", cashier);
            MerCashier merCashier = merCashierMapper.selectOneByExample(example);
            if (merCashier == null)
                return null;
            redisUtils.hashPut(PayRedisConstant.PAY_CASHIER_PREFIX + cashier, PayRedisConstant.CASHIER_KEY, merCashier, 1L, TimeUnit.DAYS);
        }
        return (MerCashier) redisUtils.hashGet(PayRedisConstant.PAY_CASHIER_PREFIX + cashier, PayRedisConstant.CASHIER_KEY);
    }

}

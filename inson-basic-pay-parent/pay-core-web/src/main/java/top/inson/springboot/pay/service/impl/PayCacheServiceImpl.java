package top.inson.springboot.pay.service.impl;

import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import top.inson.springboot.data.dao.IMerCashierMapper;
import top.inson.springboot.data.entity.MerCashier;
import top.inson.springboot.pay.constant.PayRedisConstant;
import top.inson.springboot.pay.service.IPayCacheService;
import top.inson.springboot.utils.RedisUtils;


@Service
public class PayCacheServiceImpl implements IPayCacheService {
    @Autowired
    private IMerCashierMapper merCashierMapper;


    @Autowired
    private RedisUtils redisUtils;

    private Gson gson = new GsonBuilder().create();
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
            redisUtils.hashPut(PayRedisConstant.PAY_CASHIER_PREFIX + cashier, PayRedisConstant.CASHIER_KEY, gson.toJson(merCashier));
        }
        String strCashier = redisUtils.hashGet(PayRedisConstant.PAY_CASHIER_PREFIX + cashier, PayRedisConstant.CASHIER_KEY).toString();
        return gson.fromJson(strCashier, MerCashier.class);
    }

}

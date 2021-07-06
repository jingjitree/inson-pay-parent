package top.inson.springboot.pay.service;

import top.inson.springboot.data.entity.MerCashier;

/**
 * 支付相关缓存service
 */
public interface IPayCacheService {

    MerCashier getCashier(String cashier);


}

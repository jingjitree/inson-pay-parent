package top.inson.springboot.notify.service;

import top.inson.springboot.data.entity.PayOrder;

public interface IBaseNotifyService {

    void payOrderNotify(PayOrder upOrder, String orderNo);


}

package top.inson.springboot.notify.service;

import top.inson.springboot.data.entity.PayOrder;
import top.inson.springboot.data.entity.RefundOrder;

public interface IBaseNotifyService {

    void payOrderNotify(PayOrder upOrder, String orderNo);

    void refundNotify(RefundOrder upOrder, String refundNo);

}

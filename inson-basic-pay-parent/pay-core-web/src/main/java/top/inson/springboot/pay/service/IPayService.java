package top.inson.springboot.pay.service;

import top.inson.springboot.pay.entity.dto.*;
import top.inson.springboot.pay.entity.vo.*;

public interface IPayService {

    UnifiedOrderDto unifiedOrder(UnifiedOrderVo vo) throws Exception;

    MicroPayDto microPay(MicroPayVo vo);

    RefundOrderDto refundOrder(RefundOrderVo vo);

    OrderQueryDto orderQuery(OrderQueryVo vo);

    RefundQueryDto refundQuery(RefundQueryVo vo);



}

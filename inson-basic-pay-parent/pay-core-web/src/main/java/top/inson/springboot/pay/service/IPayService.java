package top.inson.springboot.pay.service;

import top.inson.springboot.pay.entity.dto.MicroPayDto;
import top.inson.springboot.pay.entity.dto.UnifiedOrderDto;
import top.inson.springboot.pay.entity.vo.MicroPayVo;
import top.inson.springboot.pay.entity.vo.UnifiedOrderVo;

public interface IPayService {

    UnifiedOrderDto unifiedOrder(UnifiedOrderVo vo) throws Exception;

    MicroPayDto microPay(MicroPayVo vo);

}

package top.inson.springboot.pay.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import top.inson.springboot.common.exception.BadRequestException;
import top.inson.springboot.data.dao.IMerCashierMapper;
import top.inson.springboot.data.entity.MerCashier;
import top.inson.springboot.pay.entity.dto.UnifiedOrderDto;
import top.inson.springboot.pay.entity.vo.UnifiedOrderVo;
import top.inson.springboot.pay.service.IPayService;


@Slf4j
@Service
public class PayServiceImpl implements IPayService {
    @Autowired
    private IMerCashierMapper merCashierMapper;


    private Gson gson = new GsonBuilder().create();
    @Override
    public UnifiedOrderDto unifiedOrder(UnifiedOrderVo vo) throws Exception{
        log.info("主扫支付请求参数：{}", gson.toJson(vo));
        Example example = new Example(MerCashier.class);
        example.createCriteria()
                .andEqualTo("cashier", vo.getCashier());
        MerCashier merCashier = merCashierMapper.selectOneByExample(example);
        if (merCashier == null)
            throw new BadRequestException("账户不存在");


        return null;
    }

}

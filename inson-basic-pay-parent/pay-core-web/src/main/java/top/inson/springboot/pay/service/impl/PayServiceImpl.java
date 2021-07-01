package top.inson.springboot.pay.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.inson.springboot.pay.entity.vo.UnifiedOrderVo;
import top.inson.springboot.pay.service.IPayService;


@Slf4j
@Service
public class PayServiceImpl implements IPayService {


    private Gson gson = new GsonBuilder().create();
    @Override
    public void unifiedOrder(UnifiedOrderVo vo) {
        log.info("主扫支付请求参数：{}", gson.toJson(vo));


    }

}

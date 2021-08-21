package top.inson.springboot.pay.web;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.inson.springboot.common.entity.response.CommonResult;
import top.inson.springboot.common.exception.BadBusinessException;
import top.inson.springboot.pay.annotation.PayCheckSign;
import top.inson.springboot.paycommon.entity.dto.*;
import top.inson.springboot.paycommon.entity.vo.*;
import top.inson.springboot.pay.service.IPayService;
import top.inson.springboot.utils.NetUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Slf4j
@Api(tags = "支付相关接口")
@RestController
@RequestMapping(value = "/api/pay")
public class PayController {
    @Autowired
    private IPayService payService;


    @PayCheckSign
    @ApiOperation(value = "主扫接口")
    @PostMapping("/unifiedOrder")
    public CommonResult<UnifiedOrderDto> unifiedOrder(@RequestBody @Valid UnifiedOrderVo vo, HttpServletRequest request){
        try {
            String reqIp = NetUtils.getIp(request);
            vo.setReqIp(reqIp);
            log.debug("接口请求IP" + reqIp);
            return CommonResult.success(payService.unifiedOrder(vo));
        } catch (Exception e) {
            log.error("主扫下单异常", e);
            return CommonResult.fail(0, e.getMessage());
        }
    }


    @PayCheckSign
    @ApiOperation(value = "被扫接口")
    @PostMapping("/microPay")
    public CommonResult<MicroPayDto> microPay(@RequestBody @Valid MicroPayVo vo, HttpServletRequest request){
        String reqIp = NetUtils.getIp(request);
        vo.setReqIp(reqIp);
        log.debug("接口请求IP" + reqIp);
        try {
            return CommonResult.success(payService.microPay(vo));
        }catch (Exception e){
            return CommonResult.fail(0, e.getMessage());
        }
    }

    @PayCheckSign
    @ApiOperation(value = "退款接口")
    @PostMapping("/refundOrder")
    public CommonResult<RefundOrderDto> refundOrder(@RequestBody @Valid RefundOrderVo vo){
        try {
            return CommonResult.success(payService.refundOrder(vo));
        }catch (Exception e){
            return CommonResult.fail(0, e.getMessage());
        }
    }


    @PayCheckSign
    @ApiOperation(value = "订单查询")
    @PostMapping("/orderQuery")
    public CommonResult<OrderQueryDto> orderQuery(@RequestBody @Valid OrderQueryVo vo){
        try {
            return CommonResult.success(payService.orderQuery(vo));
        }catch (Exception e){
            return CommonResult.fail(0, e.getMessage());
        }
    }

    @PayCheckSign
    @ApiOperation(value = "退款查询")
    @PostMapping("/refundQuery")
    public CommonResult<RefundQueryDto> refundQuery(@RequestBody @Valid RefundQueryVo vo){
        try {
            return CommonResult.success(payService.refundQuery(vo));
        }catch (Exception e){
            return CommonResult.fail(0, e.getMessage());
        }
    }

}

package top.inson.springboot.pay.web;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.inson.springboot.common.entity.response.CommonResult;
import top.inson.springboot.pay.annotation.PayCheckSign;
import top.inson.springboot.pay.entity.vo.UnifiedOrderVo;
import top.inson.springboot.pay.service.IPayService;

import javax.servlet.http.HttpServletRequest;

@Api(tags = "支付相关接口")
@RestController
@RequestMapping(value = "/api/pay")
public class PayController {
    @Autowired
    private IPayService payService;


    @PayCheckSign
    @ApiOperation(value = "主扫接口")
    @PostMapping("/unifiedOrder")
    public CommonResult unifiedOrder(@RequestBody UnifiedOrderVo vo, HttpServletRequest request){
        payService.unifiedOrder(vo);
        return CommonResult.success();
    }


    @ApiOperation(value = "被扫接口")
    @PostMapping("/microPay")
    public CommonResult microPay(){

        return CommonResult.success();
    }

    @ApiOperation(value = "退款接口")
    @PostMapping("/refundOrder")
    public CommonResult refundOrder(){

        return CommonResult.success();
    }


    @ApiOperation(value = "订单查询")
    @PostMapping("/orderQuery")
    public CommonResult orderQuery(){

        return CommonResult.success();
    }


    @ApiOperation(value = "退款查询")
    @PostMapping("/refundQuery")
    public CommonResult refundQuery(){

        return CommonResult.success();
    }

}

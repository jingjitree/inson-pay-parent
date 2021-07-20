package top.inson.springboot.notify.web;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.inson.springboot.notify.service.IEPayNotifyService;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@Api(tags = "易票联回调")
@Slf4j
@RestController
@RequestMapping(value = "/ePayNotify")
public class EPayNotifyController {
    @Autowired
    private IEPayNotifyService ePayNotifyService;


    private final Gson gson = new GsonBuilder().create();

    @ApiOperation(value = "支付回调接口")
    @PostMapping("/notifyMe")
    public String notifyMe(@RequestBody Map<String, Object> notifyMap, HttpServletRequest request){
        String efpsSign = request.getHeader("x-efps-sign");
        log.info("渠道响应签名：" + efpsSign);

        return ePayNotifyService.notifyMe(notifyMap, efpsSign);
    }


    @ApiOperation(value = "退款回调接口")
    @PostMapping("/refundNotify")
    public String refundNotify(@RequestBody Map<String, Object> notifyMap,
                               @RequestHeader(value = "x-efps-sign") String efpsSign){
        log.info("退款回调签名：" + efpsSign);
        return ePayNotifyService.refundNotify(notifyMap, efpsSign);
    }


}

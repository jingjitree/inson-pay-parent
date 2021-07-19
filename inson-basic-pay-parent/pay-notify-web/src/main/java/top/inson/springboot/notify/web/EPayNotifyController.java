package top.inson.springboot.notify.web;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Api(tags = "易票联回调")
@Slf4j
@RestController
@RequestMapping(value = "/ePayNotify")
public class EPayNotifyController {


    @ApiOperation(value = "支付回调接口")
    @PostMapping("/notifyMe")
    public String notifyMe(){
        log.debug("支付回调接口");


        return "SUCCESS";
    }


}

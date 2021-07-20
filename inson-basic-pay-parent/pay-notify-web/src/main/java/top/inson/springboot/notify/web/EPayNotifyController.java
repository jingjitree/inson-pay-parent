package top.inson.springboot.notify.web;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@Api(tags = "易票联回调")
@Slf4j
@RestController
@RequestMapping(value = "/ePayNotify")
public class EPayNotifyController {


    private final Gson gson = new GsonBuilder().create();

    @ApiOperation(value = "支付回调接口")
    @PostMapping("/notifyMe")
    public String notifyMe(@RequestBody Map<String, Object> params, HttpServletRequest request){
        String reqJson = gson.toJson(params);
        log.info("易票联回调reqJson：{}", reqJson);
        String efpsSign = request.getHeader("x-efps-sign");
        log.info("渠道响应签名：" + efpsSign);


        return "fail";
    }


}
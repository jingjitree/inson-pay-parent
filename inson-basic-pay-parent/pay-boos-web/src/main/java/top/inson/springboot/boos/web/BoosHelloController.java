package top.inson.springboot.boos.web;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.inson.springboot.boos.security.entity.JwtAdminUsers;
import top.inson.springboot.boos.security.utils.SecurityUtils;


@Slf4j
@RestController
@RequestMapping(value = "/boos")
public class BoosHelloController {


    private final Gson gson = new GsonBuilder().create();

    @GetMapping("/hello")
    public String hello(){
        JwtAdminUsers jwtUsers = SecurityUtils.principal();
        String json = gson.toJson(jwtUsers);
        log.info("json:" + json);
        return "boos 说你好啊！";
    }


}

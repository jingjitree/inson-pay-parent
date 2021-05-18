package top.inson.springboot.boos.web;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/boos")
public class BoosHelloController {


    @GetMapping("/hello")
    public String hello(){

        return "boos 说你好啊！";
    }


}

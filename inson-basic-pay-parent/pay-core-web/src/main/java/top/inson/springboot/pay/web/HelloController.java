package top.inson.springboot.pay.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.inson.springboot.pay.service.IHelloService;

@RestController
@RequestMapping(value = "/hello")
public class HelloController {
    @Autowired
    private IHelloService helloService;


    @GetMapping("/sayHello")
    public String sayHello(@RequestParam Integer userId){
        return helloService.queryUsersById(userId);
    }


}

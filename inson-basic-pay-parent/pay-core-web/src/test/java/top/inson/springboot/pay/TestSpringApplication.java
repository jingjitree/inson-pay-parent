package top.inson.springboot.pay;


import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import top.inson.springboot.utils.RedisUtils;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestSpringApplication {
    @Autowired
    private RedisUtils redisUtils;

    @Test
    public void redisTest(){
        redisUtils.setValue("username", "jingjitree");

        String username = redisUtils.getValue("username").toString();
        log.info("存入的用户名：" + username);
    }


}

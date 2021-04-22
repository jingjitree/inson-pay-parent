package top.inson.springboot.pay.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.inson.springboot.data.dao.IUsersMapper;
import top.inson.springboot.data.entity.Users;
import top.inson.springboot.pay.service.IHelloService;

@Slf4j
@Service
public class HelloServiceImpl implements IHelloService {
    @Autowired
    private IUsersMapper usersMapper;

    private final Gson gson = new GsonBuilder().create();
    @Override
    public String queryUsersById(Integer userId) {
        Users users = usersMapper.selectByPrimaryKey(userId);
        String userJson = gson.toJson(users);
        log.info("userJson:{}", userJson);
        return userJson;
    }

}

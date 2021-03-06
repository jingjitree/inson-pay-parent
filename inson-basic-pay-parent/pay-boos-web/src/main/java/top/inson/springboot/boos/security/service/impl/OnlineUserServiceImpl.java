package top.inson.springboot.boos.security.service.impl;

import cn.hutool.core.date.DateUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.inson.springboot.boos.security.entity.JwtAdminUsers;
import top.inson.springboot.security.constants.JwtConstants;
import top.inson.springboot.security.constants.SecurityConstants;
import top.inson.springboot.security.entity.OnlineUser;
import top.inson.springboot.security.service.IOnlineUserService;
import top.inson.springboot.utils.NetUtils;
import top.inson.springboot.utils.RedisUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;


@Service
public class OnlineUserServiceImpl implements IOnlineUserService<JwtAdminUsers> {
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private JwtConstants jwtConstants;


    private final Gson gson = new GsonBuilder().create();
    @Override
    public void saveUser(JwtAdminUsers jwtAdminUsers ,String token, HttpServletRequest request) {
        String ip = NetUtils.getIp(request);
        OnlineUser onlineUser = new OnlineUser(jwtAdminUsers.getUsername(),
                ip, token, DateUtil.date());
        String prefixKey = String.format(SecurityConstants.PREFIX_USER_CACHE, token);
        redisUtils.setValueTimeout(prefixKey, gson.toJson(onlineUser),
                jwtConstants.getExpiration(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void logout(String token) {
        String redisKey = String.format(SecurityConstants.PREFIX_USER_CACHE, token);
        redisUtils.delKey(redisKey);
    }

}

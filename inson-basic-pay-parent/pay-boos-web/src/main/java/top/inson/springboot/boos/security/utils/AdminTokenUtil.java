package top.inson.springboot.boos.security.utils;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import top.inson.springboot.boos.security.entity.JwtAdminUsers;
import top.inson.springboot.security.constants.JwtConstants;
import top.inson.springboot.security.utils.JwtTokenUtil;
import top.inson.springboot.utils.RedisUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class AdminTokenUtil {
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private JwtConstants jwtConstants;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;


    /**
     * redis中token续期
     * @param token
     */
    public void checkRenewal(String tokenKey, String token){
        //毫秒
        long time = redisUtils.getExpire(tokenKey) * 1000;
        //计算过期时间
        DateTime expireDate = DateUtil.offsetMillisecond(DateUtil.date(), (int) time);
        //计算当前时间与过期时间的时间差
        long differ = expireDate.getTime() - System.currentTimeMillis();
        //如果在续期检查的范围内则续期
        if (differ <= jwtConstants.getDetect()){
            long renew = time + jwtConstants.getRenew();
            redisUtils.expire(tokenKey, renew, TimeUnit.MILLISECONDS);
        }
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        JwtAdminUsers user = (JwtAdminUsers) userDetails;
        final Date created = jwtTokenUtil.getIssuedAtDateFromToken(token);
//        final Date expiration = getExpirationDateFromToken(token);
//        如果token存在，且token创建日期 > 最后修改密码的日期 则代表token有效
        return (!jwtTokenUtil.isTokenExpired(token)
                && !jwtTokenUtil.isCreatedBeforeLastPasswordReset(created, user.getLastPasswordResetDate())
        );
    }


}

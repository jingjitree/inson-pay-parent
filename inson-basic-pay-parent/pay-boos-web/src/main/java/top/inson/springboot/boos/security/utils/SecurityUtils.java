package top.inson.springboot.boos.security.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import top.inson.springboot.boos.security.entity.JwtAdminUsers;

public class SecurityUtils {

    /**
     * 获取挡墙登录用户
     * @return
     */
    public static JwtAdminUsers principal(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(principal instanceof JwtAdminUsers)
            return (JwtAdminUsers) principal;
        return null;
    }


}

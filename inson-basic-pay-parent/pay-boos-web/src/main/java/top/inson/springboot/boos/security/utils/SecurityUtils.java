package top.inson.springboot.security.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import top.inson.springboot.security.entity.OnlineUser;

public class SecurityUtils {

    /**
     * 获取挡墙登录用户
     * @return
     */
    public static OnlineUser principal(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(principal instanceof OnlineUser)
            return (OnlineUser) principal;
        return null;
    }


}

package top.inson.springboot.boos.security.filter;

import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import top.inson.springboot.boos.security.entity.JwtAdminUsers;
import top.inson.springboot.boos.security.utils.AdminTokenUtil;
import top.inson.springboot.security.constants.SecurityConstants;
import top.inson.springboot.security.core.AbstractJwtAuthorizationTokenFilter;
import top.inson.springboot.security.entity.OnlineUser;
import top.inson.springboot.security.utils.JwtTokenUtil;
import top.inson.springboot.utils.RedisUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Slf4j
@Component
public class JwtAuthorizationTokenFilter extends AbstractJwtAuthorizationTokenFilter {
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private AdminTokenUtil adminTokenUtil;
    @Autowired
    private UserDetailsService userDetailsService;


    private final Gson gson = new GsonBuilder().create();
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = jwtTokenUtil.getToken(request);
        if(StrUtil.isNotBlank(token)){
            log.info("请求的token：" + token);
            //从redis中取出缓存
            OnlineUser onlineUser = null;
            String tokenKey = String.format(SecurityConstants.PREFIX_USER_CACHE, token);
            try {
                if (redisUtils.hasKey(tokenKey)){
                    onlineUser = gson.fromJson(redisUtils.getValue(tokenKey).toString(), OnlineUser.class);
                }
            }catch (Exception e){
                log.error("读取缓存信息异常", e);
            }
            if (onlineUser != null && SecurityContextHolder.getContext().getAuthentication() == null){
                JwtAdminUsers adminUsers = (JwtAdminUsers) userDetailsService.loadUserByUsername(onlineUser.getUserName());
                boolean valid = false;
                try {
                    if (adminTokenUtil.validateToken(token, adminUsers)) {
                        valid = true;
                    }
                }catch (ExpiredJwtException e){
                    log.info("时间过期：{}", e.getMessage());
                    valid = true;
                }
                if (valid) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(adminUsers, null, adminUsers.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    //token续期
                    adminTokenUtil.checkRenewal(tokenKey, token);
                }
            }
        }
        //请求继续
        chain.doFilter(request, response);
    }

}

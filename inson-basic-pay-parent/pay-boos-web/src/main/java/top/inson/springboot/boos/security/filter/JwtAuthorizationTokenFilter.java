package top.inson.springboot.boos.security.filter;

import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import top.inson.springboot.boos.security.entity.JwtAdminUsers;
import top.inson.springboot.security.constants.JwtConstants;
import top.inson.springboot.security.core.AbstractJwtAuthorizationTokenFilter;
import top.inson.springboot.security.utils.JwtTokenUtil;
import top.inson.springboot.utils.RedisUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;


@Slf4j
@Component
public class JwtAuthorizationTokenFilter extends AbstractJwtAuthorizationTokenFilter {
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private JwtConstants jwtConstants;
    @Autowired
    private UserDetailsService userDetailsService;


    private final Gson gson = new GsonBuilder().create();
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String token = jwtTokenUtil.getToken(request);
        log.info("请求的token：" + token);
        Map<String, Object> tokenMap = null;
        try {
            if(StrUtil.isNotBlank(token))
                tokenMap = jwtTokenUtil.getMapFromToken(token);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        if(tokenMap != null && SecurityContextHolder.getContext().getAuthentication() == null){
            JwtAdminUsers userDetails = (JwtAdminUsers) userDetailsService.loadUserByUsername((String) tokenMap.get("sub"));
            userDetails.setPassword(null);
            if(jwtTokenUtil.validateToken(token)){
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        //请求继续
        chain.doFilter(request, response);
    }

}

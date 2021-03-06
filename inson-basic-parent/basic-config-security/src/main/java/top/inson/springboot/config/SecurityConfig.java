package top.inson.springboot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import top.inson.springboot.security.annotation.AnonymousAccess;
import top.inson.springboot.security.constants.IgnoreUrlConstants;
import top.inson.springboot.security.core.AbstractJwtAuthorizationTokenFilter;
import top.inson.springboot.security.core.JwtAuthenticationEntryPoint;
import top.inson.springboot.security.service.IJwtUserDetailService;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private JwtAuthenticationEntryPoint authenticationEntryPoint;
    @Autowired
    private AbstractJwtAuthorizationTokenFilter authorizationTokenFilter;

    @Autowired
    private IJwtUserDetailService jwtUserDetailService;


    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception{
        auth
                .userDetailsService(jwtUserDetailService)
                .passwordEncoder(passwordEncoderBean());
    }

    @Autowired
    private IgnoreUrlConstants ignoreUrlConstants;

    @Bean
    GrantedAuthorityDefaults grantedAuthorityDefaults(){
        return new GrantedAuthorityDefaults("");
    }

    @Bean
    public PasswordEncoder passwordEncoderBean() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity security) throws Exception {
        Map<RequestMappingInfo, HandlerMethod> handlerMethodMap = applicationContext
                .getBean(RequestMappingHandlerMapping.class).getHandlerMethods();
        Set<String> anonymousUrls = new HashSet<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> infoEntry : handlerMethodMap.entrySet()) {
            HandlerMethod handlerMethod = infoEntry.getValue();
            AnonymousAccess anonymousAccess = handlerMethod.getMethodAnnotation(AnonymousAccess.class);
            PreAuthorize preAuthorize = handlerMethod.getMethodAnnotation(PreAuthorize.class);
            if (null != preAuthorize && preAuthorize.value().toLowerCase().contains("anonymous")) {
                anonymousUrls.addAll(infoEntry.getKey().getPatternsCondition().getPatterns());
            } else if (null != anonymousAccess && null == preAuthorize) {
                anonymousUrls.addAll(infoEntry.getKey().getPatternsCondition().getPatterns());
            }
        }

        List<String> urls = ignoreUrlConstants.getUrls();
        log.info("?????????urls???{}", urls);
        if(urls != null && urls.size() > 0){
            ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry config =
                    security.requestMatchers().anyRequest().and().authorizeRequests();
            //??????url??????
            urls.forEach(url -> config.antMatchers(url).permitAll());
        }

        security
                //??????csrf
                .csrf().disable()
                //????????????
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint).and()
                //???????????????
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                //????????????
                .authorizeRequests()
                .antMatchers(
                        HttpMethod.GET,
                        "/*.html",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js"
                ).anonymous()
                // ??????OPTIONS??????
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // ???????????????????????????url?????? ??? ?????? ??????????????????????????????????????????
                .antMatchers(anonymousUrls.toArray(new String[0])).permitAll()
                // ???????????????????????????
                .anyRequest().authenticated()
                // ??????iframe ????????????
                .and().headers().frameOptions().disable();
        security
                .addFilterBefore(authorizationTokenFilter, UsernamePasswordAuthenticationFilter.class);
    }
}

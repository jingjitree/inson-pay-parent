package top.inson.springboot.boos.web;


import cn.hutool.crypto.digest.DigestUtil;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.inson.springboot.boos.entity.vo.AdminLoginVo;
import top.inson.springboot.boos.security.entity.JwtAdminUsers;
import top.inson.springboot.common.entity.response.CommonResult;
import top.inson.springboot.security.annotation.AnonymousAccess;
import top.inson.springboot.security.service.IOnlineUserService;
import top.inson.springboot.security.utils.JwtTokenUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@Api(tags = "系统授权")
@RestController
@RequestMapping(value = "/authentication")
public class AuthenticationController {
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private IOnlineUserService<JwtAdminUsers> onlineUserService;


    @ApiOperation(value = "登录接口")
    //加上注解，接口不会被拦截
    @AnonymousAccess
    @PostMapping("/login")
    public CommonResult<Map<String, Object>> login(@RequestBody AdminLoginVo vo, HttpServletRequest request){

        JwtAdminUsers jwtUsers = (JwtAdminUsers) userDetailsService.loadUserByUsername(vo.getAccount());
        if(!jwtUsers.getPassword().equals(DigestUtil.md5Hex(vo.getPassword())))
            return CommonResult.fail("用户名或密码错误");

        //生成令牌
        String token = jwtTokenUtil.generateToken(jwtUsers);
        Map<String, Object> result = Maps.newHashMap();
        result.put("token", token);

        onlineUserService.saveUser(jwtUsers, token, request);

        return CommonResult.success(result);
    }



}

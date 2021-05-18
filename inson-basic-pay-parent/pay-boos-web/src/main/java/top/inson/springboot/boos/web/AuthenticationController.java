package top.inson.springboot.boos.web;


import cn.hutool.crypto.digest.DigestUtil;
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


@Api(tags = "系统授权")
@RestController
@RequestMapping(value = "/authentication")
public class AuthenticationController {
    @Autowired
    private UserDetailsService userDetailsService;



    @ApiOperation(value = "登录接口")
    @PostMapping("/login")
    public CommonResult login(@RequestBody AdminLoginVo vo){

        JwtAdminUsers jwtUsers = (JwtAdminUsers) userDetailsService.loadUserByUsername(vo.getAccount());
        if(!jwtUsers.getPassword().equals(DigestUtil.md5Hex(vo.getPassword())))
            return CommonResult.fail("用户名或密码错误");

        return CommonResult.success();
    }



}

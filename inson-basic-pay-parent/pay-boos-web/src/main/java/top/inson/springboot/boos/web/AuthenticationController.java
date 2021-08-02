package top.inson.springboot.boos.web;


import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.wf.captcha.base.Captcha;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import top.inson.springboot.boos.entity.vo.AdminLoginVo;
import top.inson.springboot.boos.security.entity.JwtAdminUsers;
import top.inson.springboot.boos.security.utils.SecurityUtils;
import top.inson.springboot.boos.util.captche.LoginCodeEnum;
import top.inson.springboot.boos.util.captche.LoginProperties;
import top.inson.springboot.common.entity.response.CommonResult;
import top.inson.springboot.common.exception.BadRequestException;
import top.inson.springboot.security.annotation.AnonymousAccess;
import top.inson.springboot.security.constants.JwtConstants;
import top.inson.springboot.security.service.IOnlineUserService;
import top.inson.springboot.security.utils.JwtTokenUtil;
import top.inson.springboot.utils.RedisUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Slf4j
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


    @Autowired
    private LoginProperties loginProperties;
    @Autowired
    private JwtConstants jwtConstants;
    @Autowired
    private RedisUtils redisUtils;



    @ApiOperation(value = "登录接口")
    //加上注解，接口不会被拦截
    @AnonymousAccess
    @PostMapping("/login")
    public CommonResult<Map<String, Object>> login(@RequestBody AdminLoginVo vo, HttpServletRequest request){
        //验证图形验证码
        this.validImgCode(vo);

        JwtAdminUsers jwtUsers = (JwtAdminUsers) userDetailsService.loadUserByUsername(vo.getAccount());
        if(!jwtUsers.getPassword().equals(DigestUtil.md5Hex(vo.getPassword())))
            return CommonResult.fail("用户名或密码错误");

        //生成令牌
        String token = jwtTokenUtil.generateToken(jwtUsers);
        Map<String, Object> result = MapUtil.builder(new HashMap<String, Object>())
                .put("token", token)
                .put("user", jwtUsers)
                .build();

        onlineUserService.saveUser(jwtUsers, token, request);

        return CommonResult.success(result);
    }

    @ApiOperation(value = "查询用户信息")
    @GetMapping("/getUserInfo")
    public CommonResult<JwtAdminUsers> getUserInfo(){
        return CommonResult.success(SecurityUtils.principal());
    }

    private void validImgCode(AdminLoginVo vo) {
        String code = (String) redisUtils.getValue(vo.getCodeUuid());
        if (StrUtil.isBlank(code))
            throw new BadRequestException("验证码不存在或已过期");
        if (!code.equals(vo.getCode())) {
            log.info("code: {},上送的code:{}", code, vo.getCode());
            throw new BadRequestException("验证码不正确");
        }
    }


    @ApiOperation(value = "获取图形验证码")
    @AnonymousAccess
    @GetMapping("/getCode")
    public CommonResult<Map<String, Object>> getCode(){
        Captcha captcha = loginProperties.getCaptcha();
        String codeUuid = jwtConstants.getCodeKey() + IdUtil.simpleUUID();
        //当验证码类型为 arithmetic时且长度 >= 2 时，captcha.text()的结果有几率为浮点型
        String captchaValue = captcha.text();
        if (captcha.getCharType() - 1 == LoginCodeEnum.arithmetic.ordinal() && captchaValue.contains(".")) {
            captchaValue = captchaValue.split("\\.")[0];
        }
        //存入redis中
        log.debug("验证码存入redisKey:{}，code：{}", codeUuid, captchaValue);
        redisUtils.setValueTimeout(codeUuid, captchaValue,
                loginProperties.getLoginCode().getExpiration(), TimeUnit.MINUTES);
        Map<String, Object> result = MapUtil.builder(new HashMap<String, Object>())
                .put("img", captcha.toBase64())
                .put("codeUuid", codeUuid)
                .build();
        return CommonResult.success(result);
    }


    @ApiOperation(value = "退出登录")
    @GetMapping("/logout")
    public CommonResult logout(HttpServletRequest request){
        String token = jwtTokenUtil.getToken(request);
        log.info("退出登录token:" + token);
        onlineUserService.logout(token);
        return CommonResult.success();
    }

}

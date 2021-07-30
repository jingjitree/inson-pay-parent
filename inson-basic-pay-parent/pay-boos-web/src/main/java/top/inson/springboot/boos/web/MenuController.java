package top.inson.springboot.boos.web;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.inson.springboot.boos.entity.dto.MenuDto;
import top.inson.springboot.boos.security.entity.JwtAdminUsers;
import top.inson.springboot.boos.security.utils.SecurityUtils;
import top.inson.springboot.boos.services.IMenuService;
import top.inson.springboot.common.entity.response.CommonResult;

import java.util.List;


@Slf4j
@Api(tags = "系统菜单控制器")
@RestController
@RequestMapping(value = "/menu")
public class MenuController {
    @Autowired
    private IMenuService menuService;



    private final Gson gson = new GsonBuilder().create();

    @ApiOperation(value = "查询用户菜单")
    @GetMapping("/build")
    public CommonResult<List<MenuDto>> build(){
        JwtAdminUsers adminUsers = SecurityUtils.principal();
        log.debug("登录用户：{}", gson.toJson(adminUsers));
        return CommonResult.success(menuService.buildMenu(adminUsers));
    }



}

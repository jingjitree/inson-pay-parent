package top.inson.springboot.boos.services.impl;

import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import top.inson.springboot.boos.services.IJwtPermissionService;
import top.inson.springboot.data.dao.ISysMenuMapper;
import top.inson.springboot.data.entity.AdminUsers;
import top.inson.springboot.data.entity.SysMenu;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class JwtPermissionServiceImpl implements IJwtPermissionService {
    @Autowired
    private ISysMenuMapper sysMenuMapper;


    @Override
    public Collection<GrantedAuthority> getUserGrantedAuthority(AdminUsers adminUsers) {
        List<SysMenu> menus = sysMenuMapper.selectUserRoleMenu(adminUsers.getId());
        return menus.stream()
                .filter(menu -> StrUtil.isNotBlank(menu.getPermission()))
                .map(menu -> new SimpleGrantedAuthority(menu.getPermission()))
                .collect(Collectors.toList());
    }

}

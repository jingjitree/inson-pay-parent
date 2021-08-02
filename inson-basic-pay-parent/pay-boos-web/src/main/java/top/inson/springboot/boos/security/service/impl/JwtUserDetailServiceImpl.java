package top.inson.springboot.boos.security.service.impl;

import cn.hutool.core.bean.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import top.inson.springboot.boos.security.entity.JwtAdminUsers;
import top.inson.springboot.boos.services.IJwtPermissionService;
import top.inson.springboot.common.exception.BadRequestException;
import top.inson.springboot.data.dao.IAdminUsersMapper;
import top.inson.springboot.data.entity.dto.AdminUsersDto;
import top.inson.springboot.security.service.IJwtUserDetailService;


@Service
@CacheConfig(cacheNames = "jwtUserDetail")
public class JwtUserDetailServiceImpl implements IJwtUserDetailService {
    @Autowired
    private IAdminUsersMapper adminUsersMapper;
    @Autowired
    private IJwtPermissionService jwtPermissionService;


    @Override
    @Cacheable(key = "'jwtAdminUsers:' + #p0")
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        AdminUsersDto usersDto = adminUsersMapper.selectUserByUsername(s);
        if(usersDto == null){
            throw new BadRequestException("账号不存在");
        }
        return this.createJwtAdminUser(usersDto);
    }

    private UserDetails createJwtAdminUser(AdminUsersDto usersDto) {
        JwtAdminUsers adminUsers = new JwtAdminUsers();
        BeanUtil.copyProperties(usersDto, adminUsers);
        //查询用户权限
        adminUsers.setAuthorities(jwtPermissionService.getUserGrantedAuthority(usersDto));
        return adminUsers;
    }


}

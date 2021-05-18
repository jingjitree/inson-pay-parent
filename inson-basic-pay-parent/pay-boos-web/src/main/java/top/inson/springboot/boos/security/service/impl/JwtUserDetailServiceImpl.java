package top.inson.springboot.boos.security.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import top.inson.springboot.boos.security.entity.JwtAdminUsers;
import top.inson.springboot.common.exception.BadRequestException;
import top.inson.springboot.data.dao.IAdminUsersMapper;
import top.inson.springboot.data.entity.AdminUsers;
import top.inson.springboot.security.service.IJwtUserDetailService;


@Service
public class JwtUserDetailServiceImpl implements IJwtUserDetailService {
    @Autowired
    private IAdminUsersMapper adminUsersMapper;


    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        Example example = new Example(AdminUsers.class);
        example.createCriteria()
                .andEqualTo("account", s);
        AdminUsers adminUsers = adminUsersMapper.selectOneByExample(example);
        if(adminUsers == null){
            throw new BadRequestException("账号不存在");
        }
        return createJwtAdminUser(adminUsers);
    }

    public UserDetails createJwtAdminUser(AdminUsers users){
        JwtAdminUsers jwtUsers = new JwtAdminUsers()
                .setTrueUsername(users.getUsername());
        BeanUtils.copyProperties(users, jwtUsers);
        return jwtUsers;
    }


}

package top.inson.springboot.data.dao;

import top.inson.springboot.data.base.ITKBaseMapper;
import top.inson.springboot.data.entity.AdminUsers;
import top.inson.springboot.data.entity.dto.AdminUsersDto;

public interface IAdminUsersMapper extends ITKBaseMapper<AdminUsers> {

    AdminUsersDto selectUserByUsername(String username);


}

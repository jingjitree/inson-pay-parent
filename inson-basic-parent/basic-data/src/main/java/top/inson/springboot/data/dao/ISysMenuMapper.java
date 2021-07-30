package top.inson.springboot.data.dao;

import top.inson.springboot.data.base.ITKBaseMapper;
import top.inson.springboot.data.entity.SysMenu;

import java.util.List;

public interface ISysMenuMapper extends ITKBaseMapper<SysMenu> {

    List<SysMenu> selectUserRoleMenu(Integer usersId);


}

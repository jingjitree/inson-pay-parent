package top.inson.springboot.data.dao;

import top.inson.springboot.data.base.ITKBaseMapper;
import top.inson.springboot.data.entity.SysRole;

import java.util.List;

public interface ISysRoleMapper extends ITKBaseMapper<SysRole> {

    List<SysRole> selectRoleWithUserId(Integer userId);

}

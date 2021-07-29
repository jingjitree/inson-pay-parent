package top.inson.springboot.data.entity;

import lombok.Getter;
import lombok.Setter;
import top.inson.springboot.common.entity.BaseEntity;

import javax.persistence.Table;


@Getter
@Setter
@Table(name = "sys_users_roles")
public class SysUsersRoles extends BaseEntity {

    private Integer userId;
    private Integer roleId;
    
}

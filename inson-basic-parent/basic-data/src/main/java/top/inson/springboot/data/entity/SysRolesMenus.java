package top.inson.springboot.data.entity;

import lombok.Getter;
import lombok.Setter;
import top.inson.springboot.common.entity.BaseEntity;

import javax.persistence.Table;


@Getter
@Setter
@Table(name = "sys_roles_menus")
public class SysRolesMenus extends BaseEntity {
    private Integer menuId;
    private Integer roleId;


}

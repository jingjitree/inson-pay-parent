package top.inson.springboot.data.entity;

import lombok.Getter;
import lombok.Setter;
import top.inson.springboot.common.entity.BaseEntity;

import javax.persistence.Table;


@Getter
@Setter
@Table(name = "sys_menu")
public class SysMenu extends BaseEntity {
    
    private Integer pid;
    private Integer subCount;
    private Integer type;
    private String title;
    private String name;
    private String component;
    private Integer menuSort;
    private String icon;
    private String path;
    private Boolean iFrame;
    private Boolean cache;
    private Boolean hidden;
    private String permission;
    private String createBy;
    private String updateBy;

}

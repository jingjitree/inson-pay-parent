package top.inson.springboot.data.entity;

import lombok.Getter;
import lombok.Setter;
import top.inson.springboot.common.entity.BaseEntity;

import javax.persistence.Table;


@Getter
@Setter
@Table(name = "sys_role")
public class SysRole extends BaseEntity {
    private String name;
    private Integer level;
    private String description;
    private String dataScope;
    private String createBy;
    private String updateBy;

}

package top.inson.springboot.data.entity;

import lombok.Getter;
import lombok.Setter;
import top.inson.springboot.common.entity.BaseEntity;

import javax.persistence.Table;


@Getter
@Setter
@Table(name = "admin_users")
public class AdminUsers extends BaseEntity {

    private String username;
    private String account;
    private String password;
    private String email;
    private String phone;
    private Boolean available;

}

package top.inson.springboot.data.entity;


import lombok.Getter;
import lombok.Setter;
import top.inson.springboot.data.base.BaseEntity;

import javax.persistence.Table;

@Getter
@Setter
@Table(name = "users")
public class Users extends BaseEntity {

    private String username;
    private String password;
    private String account;
    private Integer sex;
    private String phone;
    private String address;
    private Integer userType;

}

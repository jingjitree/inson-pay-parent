package top.inson.springboot.data.entity;

import lombok.Getter;
import lombok.Setter;
import top.inson.springboot.common.entity.BaseEntity;

import javax.persistence.Table;
import java.util.Date;


@Getter
@Setter
@Table(name = "admin_users")
public class AdminUsers extends BaseEntity {

    private Integer avatarId;
    private String email;
    private Boolean enabled;
    private String password;
    private String username;
    private Integer deptId;
    private String phone;
    private Integer jobId;
    private Date lastPasswordResetTime;
}

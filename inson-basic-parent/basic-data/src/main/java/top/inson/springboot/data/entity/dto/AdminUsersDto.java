package top.inson.springboot.data.entity.dto;

import lombok.Getter;
import lombok.Setter;
import top.inson.springboot.data.entity.AdminUsers;


@Getter
@Setter
public class AdminUsersDto extends AdminUsers {

    private String deptName;

    private String jobName;

}

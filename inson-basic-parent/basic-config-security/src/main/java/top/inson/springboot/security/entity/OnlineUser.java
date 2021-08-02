package top.inson.springboot.security.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class OnlineUser {

    private String userName;

    private String ip;

    private String key;

    private Date loginTime;
}

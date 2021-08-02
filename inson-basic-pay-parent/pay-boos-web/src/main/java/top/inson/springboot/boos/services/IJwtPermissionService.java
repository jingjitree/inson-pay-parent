package top.inson.springboot.boos.services;

import org.springframework.security.core.GrantedAuthority;
import top.inson.springboot.data.entity.AdminUsers;

import java.util.Collection;

public interface IJwtPermissionService {

    Collection<GrantedAuthority> getUserGrantedAuthority(AdminUsers adminUsers);


}

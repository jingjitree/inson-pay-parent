package top.inson.springboot.boos.security.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;


@Getter
@Setter
@Accessors(chain = true)
public class JwtAdminUsers implements UserDetails {

    private Integer id;
    private String trueUsername;
    private String account;
    private String password;
    private String email;
    private String phone;
    private Boolean available;
    private Date createTime;
    private Date updateTime;

    private Collection<GrantedAuthority> authorities;

    @Override
    public String getUsername() {
        return account;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }
    @Override
    public boolean isAccountNonLocked() {
        return false;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return available;
    }
}

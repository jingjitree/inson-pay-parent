package top.inson.springboot.boos.security.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Getter
@Setter
@Accessors(chain = true)
public class JwtAdminUsers implements UserDetails {
    private Integer id;

    private String username;

    private String password;

    private String avatar;

    private String email;

    private String phone;

    private String deptName;

    private String jobName;

    private boolean enabled;

    private Date createTime;
    private Date updateTime;

    private Date lastPasswordResetDate;

    @JsonIgnore
    private Collection<GrantedAuthority> authorities;

    private List<String> roles;

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

    public List<String> getRoles() {
        return authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
    }
}

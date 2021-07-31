package top.inson.springboot.security.service;

import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.http.HttpServletRequest;

public interface IOnlineUserService<T extends UserDetails> {

    void saveUser(T t, String token, HttpServletRequest request);

    void logout(String token);

}

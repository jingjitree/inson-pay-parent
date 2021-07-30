package top.inson.springboot.boos.services;

import top.inson.springboot.boos.entity.dto.MenuDto;
import top.inson.springboot.boos.security.entity.JwtAdminUsers;

import java.util.List;

public interface IMenuService {

    List<MenuDto> buildMenu(JwtAdminUsers adminUsers);


}

package top.inson.springboot.boos.services.impl;

import cn.hutool.core.convert.Convert;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;
import top.inson.springboot.boos.entity.dto.MenuDto;
import top.inson.springboot.boos.security.entity.JwtAdminUsers;
import top.inson.springboot.boos.services.IMenuService;
import top.inson.springboot.data.dao.ISysMenuMapper;
import top.inson.springboot.data.entity.SysMenu;

import java.util.List;


@Slf4j
@Service
@CacheConfig(cacheNames = "menu")
public class MenuServiceImpl implements IMenuService {
    @Autowired
    private ISysMenuMapper sysMenuMapper;


    private final Gson gson = new GsonBuilder().create();
    @Override
    public List<MenuDto> buildMenu(JwtAdminUsers adminUsers) {
        List<SysMenu> menus = sysMenuMapper.selectUserRoleMenu(adminUsers.getId());
        List<MenuDto> treeDtos = Convert.toList(MenuDto.class, menus);
        return this.buildTreeMenu(treeDtos);
    }

    private List<MenuDto> buildTreeMenu(List<MenuDto> menuDtos) {
        if (menuDtos ==null || menuDtos.size() == 0)
            return Lists.newArrayList();
        List<MenuDto> trees = Lists.newArrayList();
        menuDtos.forEach(dto -> {
            if (dto.getPid() == null)
                trees.add(dto);
            Integer pid = dto.getId();
            menuDtos.forEach(child -> {
                if (!pid.equals(child.getPid()))
                    return;
                if (dto.getChildren() == null)
                    dto.setChildren(Lists.newArrayList());
                dto.getChildren().add(child);
            });
        });
        return trees;
    }

}

package top.inson.springboot.boos.services.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.inson.springboot.boos.entity.dto.MenuDto;
import top.inson.springboot.boos.security.entity.JwtAdminUsers;
import top.inson.springboot.boos.services.IMenuService;
import top.inson.springboot.data.dao.ISysMenuMapper;
import top.inson.springboot.data.entity.SysMenu;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class MenuServiceImpl implements IMenuService {
    @Autowired
    private ISysMenuMapper sysMenuMapper;


    @Override
    public List<MenuDto> buildMenu(JwtAdminUsers adminUsers) {
        List<SysMenu> menus = sysMenuMapper.selectUserRoleMenu(adminUsers.getId());
        List<MenuDto> trees = this.buildTreeMenu(menus);


        return trees;
    }

    private List<MenuDto> buildTreeMenu(List<SysMenu> menus) {
        if (menus.size() <= 0)
            return Lists.newArrayList();
        //先取出第一级菜单目录
        List<MenuDto> datas = menus.stream().filter(m -> m.getPid() == null).map(data -> {
            MenuDto menuDto = new MenuDto();
            BeanUtil.copyProperties(data, menuDto);
            return menuDto;
        }).collect(Collectors.toList());
        //取出子类
        datas.forEach(da -> {
            Integer parentId = da.getId();
            List<MenuDto> children = da.getChildren();
            if (children == null)
                children = Lists.newArrayList();
            List<SysMenu> childMenus = menus.stream().filter(m -> m.getPid() != null && parentId.equals(m.getPid())).collect(Collectors.toList());
            List<MenuDto> childs = Convert.toList(MenuDto.class, childMenus);
            children.addAll(childs);
        });

        return datas;
    }

}

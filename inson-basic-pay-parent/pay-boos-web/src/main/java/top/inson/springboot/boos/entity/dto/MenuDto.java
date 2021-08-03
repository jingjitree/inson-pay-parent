package top.inson.springboot.boos.entity.dto;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import top.inson.springboot.boos.entity.vo.MenuMetaVo;

import java.util.List;



@Getter
@Setter
@ApiModel(value = "菜单响应实体")
public class MenuDto implements java.io.Serializable{

    private Integer id;
    private String title;
    private String path;
    private Integer pid;
    private String icon;
    private Boolean hidden;
    private Boolean cache;
    private String component;

    private String redirect;

    private Boolean alwaysShow;

    private MenuMetaVo meta;

    private List<MenuDto> children;

}

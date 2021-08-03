package top.inson.springboot.boos.entity.vo;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MenuMetaVo implements java.io.Serializable{

    private String title;

    private String icon;

    private Boolean noCache;

}

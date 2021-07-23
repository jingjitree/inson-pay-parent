package top.inson.springboot.pay.entity.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "主扫响应数据实体")
public class UnifiedOrderDto extends PayBaseDto implements java.io.Serializable {

    @ApiModelProperty(value = "付款地址")
    private String codeUrl;


}

package top.inson.springboot.paycommon.entity.dto;


import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "退款响应数据实体")
public class RefundOrderDto extends RefundBaseDto implements java.io.Serializable{

}

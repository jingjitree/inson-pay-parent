package top.inson.springboot.pay.entity.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "订单查询请求实体")
public class OrderQueryVo extends PayBaseVo implements java.io.Serializable{

    @ApiModelProperty(value = "平台订单号")
    private String orderNo;

    @ApiModelProperty(value = "商户订单编号")
    private String mchOrderNo;


}

package top.inson.springboot.pay.entity.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "主扫支付请求实体")
public class UnifiedOrderVo implements java.io.Serializable{

    @ApiModelProperty(value = "账号")
    private String cashier;

    @ApiModelProperty(value = "支付方式（1.微信，2.支付宝）")
    private Integer payType;

    @ApiModelProperty(value = "支付金额（单位：分）")
    private Integer payMoney;


}
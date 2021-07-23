package top.inson.springboot.pay.entity.vo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ApiModel(value = "被扫请求参数")
public class MicroPayVo extends PayBaseVo implements java.io.Serializable{

    @ApiModelProperty(value = "支付金额（单位：分）")
    @Min(value = 1, message = "最小支付金额一分")
    private Integer payMoney;

    @ApiModelProperty(value = "商户订单号")
    @NotBlank(message = "商户订单号不允许为空")
    private String mchOrderNo;

    @ApiModelProperty(value = "订单标题")
    @NotBlank(message = "订单标题不能为空")
    private String body;

    @ApiModelProperty(value = "订单描述")
    private String subject;

    @ApiModelProperty(value = "回调接收地址")
    @NotBlank(message = "回调地址不允许为空")
    private String notifyUrl;

    @ApiModelProperty(value = "付款码")
    @NotBlank(message = "付款码不能为空")
    private String authCode;

    @JsonIgnore
    private Integer payType;

}

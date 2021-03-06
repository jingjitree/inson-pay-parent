package top.inson.springboot.paycommon.entity.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel(value = "主扫支付请求实体")
public class UnifiedOrderVo extends PayBaseVo implements java.io.Serializable{

    @ApiModelProperty(value = "支付方式（1.微信，2.支付宝）", required = true)
    @NotNull(message = "支付方式不能为空")
    private Integer payType;

    @ApiModelProperty(value = "支付金额（单位：分）", required = true)
    @Min(value = 1, message = "最小支付金额一分")
    private Integer payMoney;

    @ApiModelProperty(value = "商户订单号", required = true)
    @NotBlank(message = "商户订单号不允许为空")
    private String mchOrderNo;

    @ApiModelProperty(value = "订单标题", required = true)
    @NotBlank(message = "订单标题不能为空")
    private String body;

    @ApiModelProperty(value = "订单描述")
    private String subject;

    @ApiModelProperty(value = "回调接收地址", required = true)
    @NotBlank(message = "回调地址不允许为空")
    private String notifyUrl;

    @ApiModelProperty(value = "同步跳转地址")
    private String returnUrl;


}

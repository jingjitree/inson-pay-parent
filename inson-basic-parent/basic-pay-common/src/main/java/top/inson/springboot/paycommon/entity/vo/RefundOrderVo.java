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
@ApiModel(value = "退款请求数据实体")
public class RefundOrderVo extends PayBaseVo implements java.io.Serializable{

    @ApiModelProperty(value = "平台订单号", required = true)
    @NotBlank(message = "支付订单号不能为空")
    private String orderNo;

    @ApiModelProperty(value = "退款订单号", required = true)
    @NotBlank(message = "退款订单号不能为空")
    private String mchRefundNo;

    @ApiModelProperty(value = "退款金额", required = true)
    @NotNull(message = "退款金额必传")
    @Min(value = 1, message = "退款金额有误")
    private Integer refundMoney;

    @ApiModelProperty(value = "退款回调地址", required = true)
    @NotBlank(message = "退款回调地址不能为空")
    private String notifyUrl;

    @ApiModelProperty(value = "退款备注字段")
    private String remark;

}

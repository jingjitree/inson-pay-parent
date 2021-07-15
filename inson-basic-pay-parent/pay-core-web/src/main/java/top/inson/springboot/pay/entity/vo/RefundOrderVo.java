package top.inson.springboot.pay.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ApiModel(value = "退款请求数据实体")
public class RefundOrderVo extends PayBaseVo implements java.io.Serializable{

    @ApiModelProperty(value = "平台订单号")
    @NotBlank(message = "支付订单号不能为空")
    private String orderNo;

    @ApiModelProperty(value = "退款订单号")
    @NotBlank(message = "退款订单号不能为空")
    private String mchRefundOrder;

}

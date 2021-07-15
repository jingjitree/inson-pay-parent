package top.inson.springboot.pay.entity.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "退款响应数据实体")
public class RefundOrderDto implements java.io.Serializable{

    @ApiModelProperty(value = "平台退款订单号")
    private String refundNo;

    @ApiModelProperty(value = "退款订单号")
    private String mchRefundNo;

    @ApiModelProperty(value = "退款金额")
    private Integer refundMoney;

    @ApiModelProperty(value = "退款状态（1.申请退款，2.退款中，3.退款成功，4.退款失败）")
    private Integer refundStatus;

    @ApiModelProperty(value = "退款描述")
    private String refundDesc;
}

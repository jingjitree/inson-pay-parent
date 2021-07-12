package top.inson.springboot.pay.entity.dto;


import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class PayBaseDto implements java.io.Serializable{

    @ApiModelProperty(value = "平台订单号")
    private String orderNo;
    @ApiModelProperty(value = "商户订单号")
    private String mchOrderNo;

    @ApiModelProperty(value = "支付账户")
    private String cashier;

    @ApiModelProperty(value = "订单状态（1.支付中，2.支付成功，3.支付失败，4.已取消，5.已退款，6.全额退款）")
    private Integer orderStatus;

    @ApiModelProperty(value = "订单描述")
    private String orderDesc;

    @ApiModelProperty(value = "订单金额（单位：分）")
    private Integer payAmount;

}

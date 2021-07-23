package top.inson.springboot.paycommon.entity.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "支付回调通知实体")
public class PayNotifyDto extends PayBaseDto implements java.io.Serializable{

    @ApiModelProperty(value = "支付方式：1.微信，2.支付宝")
    private Integer payType;

    @ApiModelProperty(value = "支付类型（1.主扫，2.被扫，3.公众号，4.小程序，5.支付宝生活号，6.app支付）")
    private Integer payCategory;

    @ApiModelProperty(value = "订单备注")
    private String remark;

}

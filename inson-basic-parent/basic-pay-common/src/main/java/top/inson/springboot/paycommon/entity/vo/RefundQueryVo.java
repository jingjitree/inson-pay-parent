package top.inson.springboot.pay.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@ApiModel(value = "退款查询请求实体")
public class RefundQueryVo extends PayBaseVo implements java.io.Serializable{

    @ApiModelProperty(value = "平台退款订单号")
    private String refundNo;



}

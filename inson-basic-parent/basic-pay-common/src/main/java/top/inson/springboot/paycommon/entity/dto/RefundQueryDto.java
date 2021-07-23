package top.inson.springboot.paycommon.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;


@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "退款查询数据实体")
public class RefundQueryDto extends RefundBaseDto implements java.io.Serializable{

    @ApiModelProperty(value = "退款时间")
    private Date refundTime;

}

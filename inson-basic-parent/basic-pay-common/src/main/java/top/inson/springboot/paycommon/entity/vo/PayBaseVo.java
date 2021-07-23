package top.inson.springboot.paycommon.entity.vo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class PayBaseVo implements java.io.Serializable{
    @ApiModelProperty(value = "账号")
    @NotBlank(message = "支付账户不能为空")
    private String cashier;

    @JsonIgnore
    private String reqIp;

}

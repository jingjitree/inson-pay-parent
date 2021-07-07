package top.inson.springboot.data.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import top.inson.springboot.common.entity.BaseEntity;

import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;


@Getter
@Setter
@Accessors(chain = true)
@Table(name = "pay_order")
public class PayOrder extends BaseEntity {

    private String orderNo;
    private String chOrderNo;
    private String preChOrderNo;
    private String mchOrderNo;
    private String cashier;
    private String merchantNo;
    private BigDecimal payAmount;
    private BigDecimal discountAmount;
    private BigDecimal orderRate;
    private BigDecimal channelRate;
    private BigDecimal feeAmount;
    private BigDecimal channelFeeAmount;
    private String channelNo;
    private Integer payType;
    private Integer payCategory;
    private String appid;
    private String openid;
    private String authorCode;
    private Integer orderStatus;
    private BigDecimal refundAmount;
    private Date payTime;

}

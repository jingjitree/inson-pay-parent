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
@Table(name = "refund_order")
public class RefundOrder extends BaseEntity {
    private String payOrderNo;
    private String refundNo;
    private String chRefundNo;
    private String mchRefundNo;
    private BigDecimal refundAmount;
    private Integer refundStatus;
    private String merchantNo;
    private Date refundTime;

}

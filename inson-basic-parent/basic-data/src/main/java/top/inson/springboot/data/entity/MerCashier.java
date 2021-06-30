package top.inson.springboot.data.entity;

import lombok.Getter;
import lombok.Setter;
import top.inson.springboot.common.entity.BaseEntity;

import javax.persistence.Table;


@Getter
@Setter
@Table(name = "mer_cashier")
public class MerCashier extends BaseEntity {

    private String cashier;
    private String merchantNo;
    private String publicKey;
    private String privateKey;
    private String signKey;
    private Integer signType;
    
    
}

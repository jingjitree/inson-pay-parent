package top.inson.springboot.pay.entity.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnifiedOrderDto {

    private String orderNo;

    private String mchOrderNo;

    private String cashier;

    private String merchantNo;


}

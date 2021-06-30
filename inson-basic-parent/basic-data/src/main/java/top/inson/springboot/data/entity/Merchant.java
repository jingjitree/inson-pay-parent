package top.inson.springboot.data.entity;

import lombok.Getter;
import lombok.Setter;
import top.inson.springboot.common.entity.BaseEntity;

import javax.persistence.Table;

@Getter
@Setter
@Table(name = "merchant")
public class Merchant extends BaseEntity {

    private String merchantNo;
    private String merchantName;
    private Integer merchantType;
    private String province;
    private String provinceName;
    private String city;
    private String cityName;
    private String area;
    private String areaName;
    private String street;
    private String streetName;
    private String address;
    private String phone;
    private String contactName;
    private String idCard;
    private String licenseCard;
    private String licenseScope;


}


package top.inson.springboot.data.entity;

import lombok.Getter;
import lombok.Setter;
import top.inson.springboot.common.entity.BaseEntity;

import javax.persistence.Table;

@Getter
@Setter
@Table(name = "channel_submer_config")
public class ChannelSubmerConfig extends BaseEntity {

    private String channelNo;
    private String merchantNo;
    private String channelSubMerNo;
    private String publicKey;
    private String privateKey;
    private String signKey;
    private String keyUrl;
    private Integer payType;

}

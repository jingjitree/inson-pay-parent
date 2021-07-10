package top.inson.springboot.data.entity;

import lombok.Getter;
import lombok.Setter;
import top.inson.springboot.common.entity.BaseEntity;

import javax.persistence.Table;

@Getter
@Setter
@Table(name = "channel_config")
public class ChannelConfig extends BaseEntity {

    private String channelNo;
    private String channelMerNo;
    private String publicKey;
    private String privateKey;
    private String channelPublicKey;
    private String signKey;
    private String keyUrl;
    private Integer payType;
}

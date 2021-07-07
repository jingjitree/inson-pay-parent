package top.inson.springboot.data.entity;

import lombok.Getter;
import lombok.Setter;
import top.inson.springboot.common.entity.BaseEntity;

import javax.persistence.Table;


@Getter
@Setter
@Table(name = "mer_channel_setting")
public class MerChannelSetting extends BaseEntity {

    private String merchantNo;
    private String channelId;
    private Boolean enable;

}

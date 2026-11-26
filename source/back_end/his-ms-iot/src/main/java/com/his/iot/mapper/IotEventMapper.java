package com.his.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.iot.entity.IotEvent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物联网设备事件（biz_iot_event）。
 */
@Mapper
public interface IotEventMapper extends BaseMapper<IotEvent> {
}

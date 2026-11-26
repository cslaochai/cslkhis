package com.his.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.iot.entity.IotDevice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物联网设备档案（biz_iot_device）——设备科台账，只做设备存在性/启停判断。
 * 「现在挂在谁身上」不在本表：见 {@link IotDeviceBindMapper}。
 */
@Mapper
public interface IotDeviceMapper extends BaseMapper<IotDevice> {
}

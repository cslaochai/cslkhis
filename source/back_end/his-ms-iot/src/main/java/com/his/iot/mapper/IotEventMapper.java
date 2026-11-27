package com.his.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.iot.entity.IotEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 物联网设备事件（biz_iot_event）。
 */
@Mapper
public interface IotEventMapper extends BaseMapper<IotEvent> {

    /**
     * 活跃告警设备聚合：未处理且级别≥2（警告/严重）的事件，按设备取最高级别。
     * 台账总览的告警状态以此为准；提示级流水（开门/任务完成/回床等）不计入告警。
     */
    @Select("SELECT device_no AS deviceNo, MAX(level) AS maxLevel FROM biz_iot_event "
            + "WHERE handled_flag = 0 AND level >= 2 GROUP BY device_no")
    List<Map<String, Object>> selectActiveAlarmLevels();
}

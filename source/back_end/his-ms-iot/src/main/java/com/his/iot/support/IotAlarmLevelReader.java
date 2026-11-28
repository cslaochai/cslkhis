package com.his.iot.support;

import com.his.iot.mapper.IotEventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 活跃告警级别只读取数：从事件表折出「设备 → 最高告警级别」。
 * <p>
 * 为什么单列叶子组件：遥测侧要把告警级别并进最新值快照，而事件侧本身依赖配送（事件回写任务单）、
 * 配送又依赖遥测（选空闲车看电量），遥测再直接注入 IotEventService 就成
 * Event→Dispatch→Telemetry→Event 的 Bean 环。这里只注入 IotEventMapper，不含任何服务依赖，
 * 环在此处断开。
 */
@Component
@RequiredArgsConstructor
public class IotAlarmLevelReader {

    private final IotEventMapper iotEventMapper;

    /**
     * 活跃告警聚合：deviceNo → 最高告警级别（2-警告 3-严重）。
     * 口径：未处理且级别≥2 的事件；提示级流水不计入。台账总览的告警状态以此为准，
     * 不在前端拿最新遥测快照二次推断。
     */
    public Map<String, Integer> activeAlarmDeviceLevels() {
        Map<String, Integer> map = new HashMap<>();
        for (Map<String, Object> row : iotEventMapper.selectActiveAlarmLevels()) {
            Object no = row.get("deviceNo");
            Object lv = row.get("maxLevel");
            if (no == null || lv == null) {
                continue;
            }
            map.put(String.valueOf(no), ((Number) lv).intValue());
        }
        return map;
    }
}

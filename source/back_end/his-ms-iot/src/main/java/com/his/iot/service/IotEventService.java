package com.his.iot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.iot.dto.IotEventHandleDTO;
import com.his.iot.dto.IotEventPageQueryDTO;
import com.his.iot.vo.IotEventVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;

/**
 * 物联网设备事件（告警）落库与处理闭环。
 */
public interface IotEventService {

    /**
     * MQTT 事件落库（幂等：device_no + seq + ts 重复时忽略），落库后按科室 SSE 推送给监护大屏。
     */
    void saveDeviceEvent(String deviceNo, String deviceType, String eventCode, Integer level, String detail,
                         String dataJson, Long seq, LocalDateTime ts);

    /**
     * 建立科室告警实时流。deptId 为空时取当前岗位科室；传入的科室必须过数据权限。
     */
    SseEmitter subscribeAlarm(Long deptId);

    /**
     * 告警分页（护士站按处理状态/时间范围查）。
     */
    IPage<IotEventVO> listPage(IotEventPageQueryDTO query);

    /**
     * 处理/忽略一条告警，落处理人与说明。只能处理「未处理」的事件。
     */
    void handle(IotEventHandleDTO dto);
}

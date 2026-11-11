package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 患者端-可选检查时段（小程序「检查预约」选时段弹层）。
 */
@Data
public class PatientExamSlotVO {

    /**
     * 设备ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 检查室
     */
    private String roomName;

    /**
     * 检查日期（yyyy-MM-dd）
     */
    private String examDate;

    /**
     * 开始时间（HH:mm）
     */
    private String startTime;

    /**
     * 结束时间（HH:mm）
     */
    private String endTime;

    /**
     * 剩余号源
     */
    private Integer availableSource;
}

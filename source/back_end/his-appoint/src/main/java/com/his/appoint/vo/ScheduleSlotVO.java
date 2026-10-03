package com.his.appoint.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 排班时间片段VO（半小时一档）
 */
@Data
public class ScheduleSlotVO {

    /**
     * 片段ID（挂号/预约选段入参 slotId）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 排班ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long scheduleId;

    /**
     * 段序（1起）
     */
    private Integer seq;

    /**
     * 段开始时间（HH:mm）
     */
    private String startTime;

    /**
     * 段结束时间（HH:mm）
     */
    private String endTime;

    /**
     * 段号源总数
     */
    private Integer totalSource;

    /**
     * 段已挂号数（现场+线上）
     */
    private Integer usedSource;

    /**
     * 段剩余号源
     */
    private Integer availableSource;

    /**
     * 段累计加号数
     */
    private Integer addedSource;

    /**
     * 段内线上预约预留（0=未划池）
     */
    private Integer appointmentSource;

    /**
     * 段内线上预约已用
     */
    private Integer usedAppointmentSource;

    /**
     * 段状态（0-停用 1-正常）
     */
    private Integer status;
}

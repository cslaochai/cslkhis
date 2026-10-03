package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 排班变更留痕出参。
 */
@Data
public class ScheduleChangeLogVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long staffScheduleId;

    private Integer actionType;
    private String actionTypeText;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromEmployeeId;

    /** 原值班人姓名（按留痕时的人快照取，人已被删时为空） */
    private String fromEmployeeName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long toEmployeeId;

    private String toEmployeeName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromShiftId;

    private String fromShiftName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long toShiftId;

    private String toShiftName;

    private Integer amount;
    private String reason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime occurTime;

    private String createBy;
}

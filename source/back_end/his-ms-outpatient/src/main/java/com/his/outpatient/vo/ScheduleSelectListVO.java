package com.his.outpatient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 排班下拉出参：挂号/复诊页选号源时真正要比对的列。
 */
@Data
@Schema(name = "ScheduleSelectListVO", description = "排班下拉出参")
public class ScheduleSelectListVO implements Serializable {

    /** 排班（号源）ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 排班人员ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /** 排班人员姓名 */
    private String doctorName;

    /** 排班日期 */
    private LocalDate scheduleDate;

    /**
     * 排班类型（号源类别）
     */
    private Integer scheduleType;

    /** 时段开始时间（如 08:00） */
    private String startTime;

    /** 时段结束时间（如 12:00） */
    private String endTime;

    /**
     * 诊室名称
     */
    private String roomName;

    /**
     * 挂号费
     */
    private BigDecimal registFee;

    /**
     * 诊查费（改约要比「号源原价 = 挂号费 + 诊查费」，只给挂号费就会把价差判错）
     */
    private BigDecimal diagnosisFee;

    /**
     * 剩余号源
     */
    private Integer availableSource;
}

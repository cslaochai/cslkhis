package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;

/**
 * 排班单元 × 日期的在岗人次（总览矩阵的格子）。
 */
@Data
public class UnitDayWorkingVO {

    /** 排班单元类型（1-科室 2-病区 3-全院） */
    private Integer orgType;

    /** 排班单元ID（全院级为 0） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    /** 排班单元名称（事实行快照） */
    private String orgName;

    /** 排班日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    /** 在岗人次（duty_status=1 的行数） */
    private Long workingCount;
}

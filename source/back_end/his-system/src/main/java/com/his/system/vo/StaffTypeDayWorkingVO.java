package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

/**
 * 岗位类别 × 日期的在岗人次（总览卡片）。
 */
@Data
public class StaffTypeDayWorkingVO {

    /** 岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他） */
    private Integer staffType;

    /** 排班日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    /** 在岗人次（duty_status=1 的行数） */
    private Long workingCount;
}

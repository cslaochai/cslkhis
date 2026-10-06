package com.his.appoint.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

/**
 * 排班总览：岗位类别 × 日期的在岗人次（卡片行）。
 */
@Data
public class OverviewStaffTypeDayVO {

    /**
     * 岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他）
     */
    private Integer staffType;

    /**
     * 排班日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    /**
     * 在岗人次
     */
    private Long workingCount;
}

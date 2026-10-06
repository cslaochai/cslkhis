package com.his.appoint.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

/**
 * 排班总览：某天的总值班解析结果（主班优先、副班顶上，found=0 表示漏排）。
 */
@Data
public class OverviewDutyDayVO {

    /**
     * 值班日期（夜班以开始日为准）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dutyDate;

    /**
     * 班次：1-白班 2-夜班
     */
    private Integer shiftType;

    /**
     * 班次文案
     */
    private String shiftTypeText;

    /**
     * 是否排到（1-有 0-漏排）
     */
    private Integer found;

    /**
     * 实际值班人（换班后为准）
     */
    private String actualEmpName;

    /**
     * 漏排原因（found=0 时给出）
     */
    private String emptyReason;
}

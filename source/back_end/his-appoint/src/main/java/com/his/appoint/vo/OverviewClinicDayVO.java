package com.his.appoint.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 排班总览：门诊号源按日汇总。
 */
@Data
public class OverviewClinicDayVO {

    /** 排班日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    /** 出诊班次数 */
    private Long shiftCount;

    /** 总号源 */
    private BigDecimal totalSource;

    /** 已挂号源 */
    private BigDecimal usedSource;

    /** 停诊班次数 */
    private Long stoppedCount;
}

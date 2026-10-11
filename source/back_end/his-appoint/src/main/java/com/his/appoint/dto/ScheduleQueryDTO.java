package com.his.appoint.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 排班查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleQueryDTO extends PageParam {
    /**
     * 科室ID
     */
    private Long deptId;
    /**
     * 排班人员ID（医生/护士/技师…同一列，见 {@code BizClinicSource#doctorId}）
     */
    private Long doctorId;

    /**
     * 岗位类别（1医生 2护理 3医技 4药学 5收费 6行政其他）：空=全部岗位。
     * 排班面板按岗位分开看——「今天内科几个医生出诊」和「今天窗口几个收费员在岗」是两张表。
     */
    private Integer staffType;

    /**
     * 查询开始日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 查询结束日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}

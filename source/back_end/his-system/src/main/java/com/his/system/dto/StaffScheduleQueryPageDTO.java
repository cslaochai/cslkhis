package com.his.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 全院岗位排班分页查询入参（日期区间 + 单元 + 岗位类别 + 出勤状态 + 关键词）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StaffScheduleQueryPageDTO extends PageParam {

    /**
     * 开始日期（含）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 结束日期（含）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 排班单元类型（1-科室 2-病区 3-全院）
     */
    private Integer orgType;

    /**
     * 排班单元ID
     */
    private Long orgId;

    /**
     * 科室ID（按科室汇总时用，不受单元类型限制）
     */
    private Long deptId;

    /**
     * 岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他）
     */
    private Integer staffType;

    /**
     * 出勤状态（1-上班 2-休息 3-请假 4-培训 5-停班）
     */
    private Integer dutyStatus;

    /**
     * 是否出诊（0-否 1-是）
     */
    private Integer clinicFlag;

    /**
     * 关键词（姓名/工号模糊匹配）
     */
    private String keyword;
}

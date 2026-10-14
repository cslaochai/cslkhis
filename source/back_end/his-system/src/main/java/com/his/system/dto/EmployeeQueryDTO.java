package com.his.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 员工分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeeQueryDTO extends PageParam {

    /**
     * 员工姓名，模糊匹配
     */
    private String empName;

    /**
     * 主科室ID，关联科室表
     * <p>默认口径：过滤员工主科室（或配合 {@link #staffType} 过滤岗位所在科室）。
     * 仅当 {@link #onlyScheduled}=true 时，语义切换为「排班的出诊科室」（见下）。
     */
    private Long deptId;

    /**
     * 员工类型：1-医生 2-护士 3-收费员 4-药剂师 5-管理员 6-其他
     */
    private Integer empType;

    /**
     * 岗位类别（1医生 2护理 3医技 4药学 5收费 6行政其他
     */
    private Integer staffType;

    /**
     * 是否只返回指定区间内有门诊排班的员工（true-是）。
     * <p>空=保持原口径（全员 / 按主科室全量），所有存量调用方零影响。
     * <p>true 时按 `biz_clinic_source`（del_flag=0）过滤：区间内该医生存在排班记录才返回，
     * 用于挂号看板等「行由排班数据驱动」的场景 —— 不再拉全员名册。
     * 此时 {@link #deptId} 过滤的是<b>排班的出诊科室</b>（cs.dept_id），不是员工主科室；
     * 不传 deptId 且当前岗位受限时，收口到授权科室内的排班。
     */
    private Boolean onlyScheduled;

    /**
     * 排班日期区间起（仅配合 {@link #onlyScheduled}=true 使用，可单边省略）
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 排班日期区间止（仅配合 {@link #onlyScheduled}=true 使用，可单边省略）
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}

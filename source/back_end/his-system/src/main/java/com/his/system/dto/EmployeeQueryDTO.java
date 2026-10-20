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

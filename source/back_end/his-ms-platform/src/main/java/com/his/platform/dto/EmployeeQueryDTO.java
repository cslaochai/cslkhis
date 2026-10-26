package com.his.platform.dto;

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
     * 角色编码（精确到某个岗位角色，比 {@link #staffType} 更窄）。
     *
     * <p>手术排台选人专用：麻醉医师候选 = 角色编码 10041（麻醉医师）、
     * 巡回/器械护士候选 = 10042（手术室护士）。
     * 走 {@code sys_employee_post} 的岗位行判定（含岗位有效期），多岗位员工也能取到。
     * 用 role_code 而不是角色自增 id —— id 随库重建会变，role_code 是人定的稳定标识。
     * 为空=不过滤，保持原行为不动存量调用方。
     */
    private String roleCode;

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

    /**
     * 排班域候选口径（1-岗位出勤 2-急诊值班 3-门诊出诊）。
     *
     * <p>排班页选人下拉专用：1=排除医生/护理/护工岗与急诊科室员工（员工排班页），
     * 2=只出急诊科室的医生（急诊排班页），
     * 3=排除护理/护工岗与急诊科室员工、医生放行（门诊排班页标休息/请假用）。
     * 为空=不过滤，保持原行为不动存量调用方。
     */
    private Integer scheduleScope;
}

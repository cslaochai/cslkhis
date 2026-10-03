package com.his.system.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
     *
     * <p>⚠️ 这是员工档案上的冗余分类，<b>与岗位表员工岗位不一致</b>，
     * 排班/授权这类要「人在哪个科室是什么岗位」的场景一律用 {@link #staffType}，不要用这一列。
     */
    private Integer empType;

    /**
     * 岗位类别（1医生 2护理 3医技 4药学 5收费 6行政其他，见 {@code StaffTypeEnum}，sql/195）
     *
     * <p>按 <b>人事岗位关系</b> 过滤：员工岗位(人 × 科室 × 角色) 联角色.staff_type。
     * 传了它，deptId 就按「岗位所在科室」过滤（而不是员工档案上的主科室快照）——
     * 排班要的是「这个科室今天有哪些护士可以排」，不是「档案主科室在这个科的护士」。
     */
    private Integer staffType;
}

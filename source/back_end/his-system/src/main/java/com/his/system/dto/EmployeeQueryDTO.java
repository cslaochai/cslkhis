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
     */
    private Integer empType;

    /**
     * 岗位类别（1医生 2护理 3医技 4药学 5收费 6行政其他
     */
    private Integer staffType;
}

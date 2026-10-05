package com.his.system.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 科室查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DepartmentQueryDTO extends PageParam {

    /**
     * 科室名称，模糊匹配
     */
    private String deptName;

    /**
     * 科室类型（1-门诊科室 2-医技科室 3-药房 4-住院科室 5-其他）
     */
    private Integer deptType;
}

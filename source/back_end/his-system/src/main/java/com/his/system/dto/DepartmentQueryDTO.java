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
     * 科室类型过滤（支持单个值或逗号分隔的多个值）
     */
    private String deptType;
}

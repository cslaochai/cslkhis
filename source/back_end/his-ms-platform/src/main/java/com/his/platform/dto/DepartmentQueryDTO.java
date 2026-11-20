package com.his.platform.dto;

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

    /**
     * 节点类型过滤（单值：1-管理片区 2-临床科室 3-医技科室 4-药剂科室 5-行政后勤 6-医疗辅助 7-其他）
     */
    private String nodeType;
}

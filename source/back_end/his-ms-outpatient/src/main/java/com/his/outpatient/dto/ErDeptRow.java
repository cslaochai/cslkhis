package com.his.outpatient.dto;

import lombok.Data;

/**
 * 急诊科室行（sys_department 按 dept_type 含 6-急诊科室 查出的跨模块只读投影）。
 * <p>带 parent_id 是为了区分「叶子急诊单元」与「虚父级」：
 * 集合内被其他急诊科室指为父级的科室（如急危重症中心）不参与公开表/占比统计。
 */
@Data
public class ErDeptRow {

    /**
     * 科室 ID
     */
    private Long id;

    /**
     * 父科室 ID（sys_department.parent_id）
     */
    private Long parentId;
}

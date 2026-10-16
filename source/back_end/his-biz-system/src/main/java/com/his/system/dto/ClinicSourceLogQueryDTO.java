package com.his.system.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 门诊排班变更留痕分页查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClinicSourceLogQueryDTO extends PageParam {

    /**
     * 科室ID（按排班归属过滤，空则不限）
     */
    private Long deptId;

    /**
     * 排班日期起
     */
    private String startDate;

    /**
     * 排班日期止
     */
    private String endDate;

    /**
     * 变更动作（1-新增排班 2-修改排班 3-删除排班 4-停诊 5-启用 6-加号 7-批量退号 8-段级编辑 9-复刻）
     */
    private Integer actionType;
}

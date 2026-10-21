package com.his.outpatient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 「待我审批」查询入参：返回当前登录人作为指定审核人的待审核排班（护理 + 全院岗位）。
 * 默认按分页返回，可选按单元类型/单元进一步收窄。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StaffSchedulePendingQueryDTO extends PageParam {

    /**
     * 排班单元类型（1-科室 2-病区 3-全院）；不传则不过滤
     */
    private Integer orgType;

    /**
     * 排班单元ID（全院级不传）
     */
    private Long orgId;
}

package com.his.system.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 人力配置标准分页查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StaffPlanRuleQueryPageDTO extends PageParam {

    /**
     * 排班单元类型（1-科室 2-病区 3-全院）
     */
    private Integer orgType;

    /**
     * 排班单元ID
     */
    private Long orgId;

    /**
     * 岗位类别
     */
    private Integer staffType;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}

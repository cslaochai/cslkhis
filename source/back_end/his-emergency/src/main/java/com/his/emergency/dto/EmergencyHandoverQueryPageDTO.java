package com.his.emergency.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 急诊交班台账分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmergencyHandoverQueryPageDTO extends PageParam {

    /**
     * 科室（可选）
     */
    private Long deptId;

    /**
     * 关键字（模糊匹配：交班单号 / 交出人 / 接班人 / 急诊号与患者名所在的明细）
     */
    private String keyword;
}

package com.his.patient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 营养膳食月度指标快照分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NutritionStatsQueryPageDTO extends PageParam {

    /** 统计月份 yyyy-MM */
    private String statMonth;

    /** 统计范围（1-全院 2-科室） */
    private Integer scopeType;

    /** 科室ID */
    private Long deptId;
}

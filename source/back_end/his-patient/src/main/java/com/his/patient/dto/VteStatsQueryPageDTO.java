package com.his.patient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * VTE 防控月度指标快照分页查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VteStatsQueryPageDTO extends PageParam {

    /** 统计月份 yyyy-MM */
    private String statMonth;

    /** 统计范围（1-全院 2-科室） */
    private Integer scopeType;
}

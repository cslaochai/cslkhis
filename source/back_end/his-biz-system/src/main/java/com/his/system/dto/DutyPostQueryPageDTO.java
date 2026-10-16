package com.his.system.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 值守点位分页查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DutyPostQueryPageDTO extends PageParam {

    /**
     * 责任范围（1-全院行政 2-急诊 3-感染 4-总务 5-信息）
     */
    private Integer dutyScope;

    /**
     * 排班单元类型（1-科室 2-病区 3-全院）
     */
    private Integer orgType;

    /**
     * 排班单元ID
     */
    private Long orgId;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;

    /**
     * 关键词（编码/名称模糊匹配）
     */
    private String keyword;
}

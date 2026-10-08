package com.his.emr.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 三级质控流转单分页查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RecordQcFlowQueryPageDTO extends PageParam {

    /**
     * 流转单号（模糊）
     */
    private String flowNo;

    /**
     * 流转状态（1-科级待审 2-病案室待审 3-医务处待审 4-终审通过 5-整改中）
     */
    private Integer flowStatus;

    /**
     * 当前停留级（1-科级 2-病案室 3-医务处）
     */
    private Integer currentLevel;

    /**
     * 病历来源（OUTPATIENT/INPATIENT）
     */
    private String recordSource;

    /**
     * 关键词（单号/患者/科室）
     */
    private String keyword;
}

package com.his.medicaltech.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报告查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReportRecordQueryDTO extends PageParam {
    /**
     * 患者ID（可选）
     */
    private Long patientId;

    /**
     * 报告类型：1-检查报告 2-检验报告（可选）
     */
    private Integer reportType;

    /** 报告状态（1-待审核 2-初审通过 3-已审核 4-已发布 5-已作废） */
    private Integer reportStatus;
}

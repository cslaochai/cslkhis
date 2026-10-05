package com.his.emr.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 质控检查记录查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QcQueryPageDTO extends PageParam {

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 病历来源（OUTPATIENT / INPATIENT）
     */
    private String recordSource;

    /**
     * 质控类型（0-综合 1-完整性检查 2-规范性检查 3-逻辑性检查 4-AI内涵质控）
     */
    private Integer qcType;

    /**
     * 质控状态：1-待处理 2-已处理 3-已忽略
     */
    private Integer qcStatus;

    /**
     * 质控结果：0-不通过 1-通过
     */
    private Integer qcResult;

    /**
     * 关键词：质控单号 / 病历号 / 患者姓名
     */
    private String keyword;
}

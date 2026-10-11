package com.his.emr.dto;

import lombok.Data;

/**
 * 执行质控检查入参
 */
@Data
public class QcExecuteDTO {

    /**
     * 病历来源（OUTPATIENT-门诊病历 INPATIENT-住院文书）；为空按门诊病历处理
     */
    private String recordSource;

    /**
     * 病历ID（门诊为门诊病历的ID，住院为住院病历文书的ID）
     */
    private Long recordId;

    /**
     * 质控类型：为空或 0 表示一次跑完完整性/规范性/逻辑性；1/2/3 表示只跑对应维度。
     * 这里**不接受 4** —— AI 内涵质控走 his-ai 的独立接口，传 4 直接报错而不是静默降级。
     */
    private Integer qcType;

}

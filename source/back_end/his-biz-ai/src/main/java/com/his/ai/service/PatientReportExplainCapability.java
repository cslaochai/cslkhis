package com.his.ai.service;

import com.his.ai.dto.PatientReportExplainDTO;
import com.his.ai.vo.PatientReportExplainVO;

/**
 * 患者端报告解读能力（大白话版）。
 */
public interface PatientReportExplainCapability {

    /**
     * 解读一份检验报告。
     *
     * @param dto 报告ID（患者身份服务端取，入参不收 patientId）
     * @return 解读结果；模型不可用仅影响措辞，事实与白话照常返回
     */
    PatientReportExplainVO execute(PatientReportExplainDTO dto);
}

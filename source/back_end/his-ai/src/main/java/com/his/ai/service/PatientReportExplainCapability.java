package com.his.ai.service;

import com.his.ai.dto.PatientReportExplainDTO;
import com.his.ai.vo.PatientReportExplainVO;

/**
 * 患者端报告解读能力（大白话版）。
 * <p>
 * 与 {@link LabInterpretCapability} 面向医生不同，本能力面向<b>患者本人</b>：
 * 只解释「这项查什么 / 你的值 / 参考范围 / 高或低通常意味着什么」，
 * 不给诊断、不给用药建议、不给处置分级。
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

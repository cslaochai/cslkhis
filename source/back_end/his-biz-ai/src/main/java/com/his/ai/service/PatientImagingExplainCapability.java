package com.his.ai.service;

import com.his.ai.dto.PatientImagingExplainDTO;
import com.his.ai.vo.PatientImagingExplainVO;

/**
 * 患者端影像报告解读能力（大白话版，G-17）。
 */
public interface PatientImagingExplainCapability {

    /**
     * 解读一份检查报告（CT/B超/放射/心电）。
     *
     * @param dto 报告ID（患者身份服务端取，入参不收 patientId）
     * @return 解读结果；模型不可用时白话串讲缺位，词典/事实/引导照常返回
     */
    PatientImagingExplainVO execute(PatientImagingExplainDTO dto);
}

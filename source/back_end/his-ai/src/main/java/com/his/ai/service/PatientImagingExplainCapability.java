package com.his.ai.service;

import com.his.ai.dto.PatientImagingExplainDTO;
import com.his.ai.vo.PatientImagingExplainVO;

/**
 * 患者端影像报告解读能力（大白话版，G-17）。
 * <p>
 * 只解读报告、不做诊断结论（NMPA 三类证红线）：把检查报告的描述/结论原文
 * 串讲成患者能懂的话，阴阳性/危急值等事实由代码给出，模型不得改写。
 * 与 {@link PatientReportExplainCapability}（检验）分开建能力：读者同为患者，
 * 但数据形态完全不同（逐项数值 vs 叙事文本），提示词与输出契约不共用（纪律 8）。
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

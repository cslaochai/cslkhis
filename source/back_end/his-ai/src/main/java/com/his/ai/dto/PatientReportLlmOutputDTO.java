package com.his.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 患者端报告解读的模型输出结构（与 prompts/patient-report-explain.md 的 JSON 契约对应）。
 * <p>
 * <b>模型在这里的权限被压缩到最小：</b>它只能输出一段 {@code summary}。
 * 逐项的白话解释（{@code items}）由 {@code sys_lab_plain_item} 词典给出，
 * 模型不得修改、不得新增 —— 因为那些文字会直接被患者当成「医院说的」。
 * <p>
 * 甚至连 {@code summary} 也要过 {@code PatientTextGuard} 的硬闸才被采用：
 * 命中诊断性/用药性/绝对化表述就整体丢弃，回落到规则文案。
 * 这不是不信任模型，是这个场景下没有医生复核环节，闸门必须放在最前面。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientReportLlmOutputDTO {

    /**
     * 面向患者的一句话总览，不超过 80 字。
     * 只描述「有几项、几项不在范围内」，不下结论、不给处置。
     */
    private String summary;
}

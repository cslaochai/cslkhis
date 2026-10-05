package com.his.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 患者端影像报告解读的模型输出结构（与 prompts/patient-imaging-explain.md 的 JSON 契约对应）。
 * <p>
 * 模型只做「白话串讲」：把报告里已有的描述/结论原文翻译成患者能懂的话，
 * 不判断、不补充、不升级。四个字段逐段过 {@code PatientTextGuard}，
 * 命中越界表述的字段单独丢弃，不影响其他段落——影像解读各段独立成立，
 * 没必要因为一段越界把整份解读丢掉。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientImagingLlmOutputDTO {

    /** 检查介绍的白话补充（仅当词典未收录该项目时采用） */
    private String examIntro;

    /** 报告「描述」部分的白话串讲 */
    private String findingsPlain;

    /** 报告「结论」部分的白话串讲 */
    private String conclusionsPlain;

    /** 报告「建议」部分的白话串讲 */
    private String advicePlain;
}

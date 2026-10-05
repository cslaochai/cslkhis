package com.his.ai.dto;

import lombok.Data;

/**
 * 危重预警模型输出（G-12）。
 * <p>契约见 prompts/deterioration-alert.md：模型只产出一段观察与上报建议，
 * <b>预警级与评分由代码给定，模型无权改</b>；advice 是临床文本，审计落指纹。</p>
 */
@Data
public class DeteriorationLlmOutputDTO {

    /**
     * 观察与上报建议（≤150 字；禁诊断、禁处置医嘱）
     */
    private String advice;
}

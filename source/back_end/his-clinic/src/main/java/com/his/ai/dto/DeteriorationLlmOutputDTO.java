package com.his.ai.dto;

import lombok.Data;

/**
 * 危重预警模型输出（G-12）。
 */
@Data
public class DeteriorationLlmOutputDTO {

    /**
     * 观察与上报建议（≤150 字；禁诊断、禁处置医嘱）
     */
    private String advice;
}

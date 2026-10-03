package com.his.ai.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 检验结果解读入参
 */
@Data
public class LabInterpretExecuteDTO {

    /**
     * 检验记录ID
     */
    @NotNull(message = "检验记录ID不能为空")
    private Long recordId;

    /**
     * 是否把结论草稿写回检验记录的诊断结论与建议字段。
     * <p>
     * <b>默认 false</b>，且刻意如此：这两个字段是检验技师写的结论，会进报告、进病历。
     * 让模型默认覆盖它，等于默认让概率系统写病历。
     * 需要写回时必须显式传 true，并且写回前会清空原有内容的行为也只在此时发生。
     */
    private Boolean overwriteConclusion;

    /**
     * 是否纳入历史结果做趋势分析，默认 true
     */
    private Boolean includeTrend;
}

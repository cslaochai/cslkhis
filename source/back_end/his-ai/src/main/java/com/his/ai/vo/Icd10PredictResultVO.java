package com.his.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * ICD-10 推荐结果。
 */
@Data
@Schema(description = "ICD-10 推荐结果")
public class Icd10PredictResultVO {

    /**
     * 是否降级
     */
    @Schema(description = "是否已降级：true 表示未经过模型，结果为规则匹配")
    private boolean degraded;

    /**
     * 降级原因
     */
    @Schema(description = "降级原因，未降级时为空")
    private String degradeReason;

    @Schema(description = "本次下发给模型的候选编码数量")
    private int candidateCount;

    @Schema(description = "推荐结果，按置信度降序")
    private List<Icd10PredictItemVO> predictions = new ArrayList<>();

    /**
     * 耗时（毫秒）
     */
    @Schema(description = "耗时（毫秒）")
    private long latencyMs;
}

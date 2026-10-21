package com.his.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 患者端导诊口语归一结果。
 */
@Data
@Schema(description = "患者端导诊口语归一结果")
public class PatientTriageNormalizeVO {

    /**
     * 归一后的检索文本。模型不可用时等于患者原话 —— 前端不用分支处理，照传即可。
     */
    @Schema(description = "用于查询导诊规则的检索文本")
    private String searchText;

    /**
     * 归一出的标准症状词（模型不可用时为空数组）
     */
    @Schema(description = "归一出的标准症状词")
    private List<String> terms;

    /**
     * 补充追问（模型不可用时为空数组）
     */
    @Schema(description = "补充追问")
    private List<String> followUps;

    /**
     * 来源：model-模型给出；rule-回落到原话
     */
    @Schema(description = "来源（model/rule）")
    private String source;

    /**
     * 是否降级（模型不可用或输出未过闸）
     */
    @Schema(description = "是否降级")
    private boolean degraded;

    /**
     * 降级原因（未降级时为空串）
     */
    @Schema(description = "降级原因")
    private String degradeReason;
}

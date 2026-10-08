package com.his.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 病历草拟结果。
 */
@Data
@Schema(description = "病历草拟结果")
public class EmrDraftResultVO {

    @Schema(description = "现病史草稿；模型未参与或无可用信息时为空字符串")
    private String presentIllness = "";

    @Schema(description = "还缺哪些要素才够写一份规范现病史（给医生的待办，不是病历正文）")
    private List<String> missingPoints = new ArrayList<>();

    /**
     * 小结
     */
    @Schema(description = "这份草稿的依据与局限")
    private String summary;

    @Schema(description = "恒为 true：本产出是草稿，必须由医生确认后采纳")
    private boolean draft = true;

    /**
     * 是否降级
     */
    @Schema(description = "模型是否未参与（true 时没有草稿可给）")
    private boolean degraded;

    /**
     * 降级原因
     */
    @Schema(description = "降级原因")
    private String degradeReason;

    /**
     * 耗时（毫秒）
     */
    @Schema(description = "耗时（毫秒）")
    private long latencyMs;
}

package com.his.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 病历文本结构化抽取结果。
 * <p>
 * <b>本结果不落库、不自动写回病历。</b> 前端必须让医生逐字段确认后再填入表单，
 * 采纳与否由医生点击决定。
 */
@Data
@Schema(description = "病历文本结构化抽取结果")
public class EmrExtractResultVO {

    @Schema(description = "抽出的字段（按病历字段顺序排列，来源为按原文标签切分的排在前面）")
    private List<EmrExtractFieldVO> fields = new ArrayList<>();

    @Schema(description = "抽出的字段数")
    private int fieldCount;

    @Schema(description = "被丢弃的条目数（证据在原文中查不到 / 字段不在白名单内 / 取值非法）")
    private int rejectedCount;

    @Schema(description = "丢弃原因（最多 5 条，便于判断是模型在编还是原文本身有问题）")
    private List<String> rejectedNotes = new ArrayList<>();

    @Schema(description = "输入是否因超长被截断")
    private boolean truncated;

    /**
     * 是否降级
     */
    @Schema(description = "模型是否未参与（true 时结果是纯规则切分）")
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

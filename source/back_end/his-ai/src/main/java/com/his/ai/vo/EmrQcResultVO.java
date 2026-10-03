package com.his.ai.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 病历内涵质控结果。
 */
@Data
@Schema(description = "病历内涵质控结果")
public class EmrQcResultVO {

    /**
     * 是否降级
     */
    @Schema(description = "是否已降级：true 表示模型未参与，仅必填项规则结论")
    private boolean degraded;

    /**
     * 降级原因
     */
    @Schema(description = "降级原因，未降级时为空")
    private String degradeReason;

    @Schema(description = "是否合格：存在 severity=3 的问题即不合格")
    private boolean pass;

    /**
     * 问题数
     */
    @Schema(description = "问题总数")
    private int issueCount;

    @Schema(description = "最高严重程度，无问题时为 0")
    private int maxSeverity;

    /**
     * 小结
     */
    @Schema(description = "整体评价")
    private String summary;

    @Schema(description = "问题列表，按严重程度降序")
    private List<EmrQcIssueVO> issues = new ArrayList<>();

    @Schema(description = "写入 biz_quality_control 的记录ID，未落库时为空")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long qcId;

    @Schema(description = "质控单号，未落库时为空")
    private String qcNo;

    /**
     * 耗时（毫秒）
     */
    @Schema(description = "耗时（毫秒）")
    private long latencyMs;
}

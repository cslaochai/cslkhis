package com.his.ai.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 医保审核证据判定出参（G-07）。
 */
@Data
public class InsuranceEvidenceVO {

    /**
     * 合规审核记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long auditId;

    /**
     * 结算清单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long settlementId;

    /**
     * 结算清单号
     */
    private String settlementNo;

    /**
     * 总评（模型产出，≤120 字）
     */
    private String overall;

    /**
     * 逐条判定（与命中规则按规则码对齐）
     */
    private List<InsuranceEvidenceJudgmentVO> judgments = new ArrayList<>();

    /**
     * true = 模型不可用/产出不可用，未给出判定
     */
    private Boolean degraded;

    /**
     * 降级原因（说人话）
     */
    private String degradeReason;
}

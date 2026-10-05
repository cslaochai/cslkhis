package com.his.ai.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 医保审核证据判定出参（G-07）。
 * <p>只读产物不落库：规则结论照旧，本 VO 只是叠加一层「证据与怀疑的关系」提示人工复核；
 * degraded=true 表示模型未产出判定（items 为空），规则的判定依据与整改建议照常在页面上。</p>
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

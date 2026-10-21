package com.his.ai.vo;

import lombok.Data;

/**
 * 医保审核证据判定逐条出参（G-07）。
 */
@Data
public class InsuranceEvidenceJudgmentVO {

    /**
     * 命中规则码（与合规审核明细对齐）
     */
    private String ruleCode;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 判定码值：1-证据支持 2-证据反驳 3-证据不足
     */
    private Integer verdict;

    /**
     * 判定文案
     */
    private String verdictText;

    /**
     * 理由（模型产出，≤80 字）
     */
    private String reason;

    /**
     * 原文引用（模型逐字摘自证据，找不到为空）
     */
    private String quote;
}

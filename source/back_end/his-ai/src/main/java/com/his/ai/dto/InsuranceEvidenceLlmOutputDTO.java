package com.his.ai.dto;

import com.his.ai.support.AiAuditPlain;
import lombok.Data;

import java.util.List;

/**
 * 医保审核证据判定模型输出（G-07）。
 * <p>契约见 prompts/insurance-evidence.md：judgments 逐条对应命中规则码，
 * verdict 只允许 supported / refuted / insufficient；模型编造的规则码与非法 verdict 由能力层丢弃。</p>
 * <p>审计口径（G-11）：规则码/verdict 是无隐私码值，标 {@link AiAuditPlain} 落审计明文作排障锚点；
 * overall/reason/quote 是临床文本，落指纹。</p>
 */
@Data
public class InsuranceEvidenceLlmOutputDTO {

    private List<LlmJudgment> judgments;

    /** 总评（≤120 字，禁止处置建议） */
    private String overall;

    @Data
    public static class LlmJudgment {

        /** 命中规则码 */
        @AiAuditPlain
        private String ruleCode;

        /** supported / refuted / insufficient */
        @AiAuditPlain
        private String verdict;

        /** 理由（≤80 字） */
        private String reason;

        /** 原文引用（≤60 字，逐字摘自证据） */
        private String quote;
    }
}

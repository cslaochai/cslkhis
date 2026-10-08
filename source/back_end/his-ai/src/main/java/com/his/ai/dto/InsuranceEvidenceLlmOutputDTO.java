package com.his.ai.dto;

import com.his.ai.support.AiAuditPlain;
import lombok.Data;

import java.util.List;

/**
 * 医保审核证据判定模型输出（G-07）。
 */
@Data
public class InsuranceEvidenceLlmOutputDTO {

    private List<LlmJudgment> judgments;

    /**
     * 总评（≤120 字，禁止处置建议）
     */
    private String overall;

    @Data
    public static class LlmJudgment {

        /**
         * 命中规则码
         */
        @AiAuditPlain
        private String ruleCode;

        /**
         * supported / refuted / insufficient
         */
        @AiAuditPlain
        private String verdict;

        /**
         * 理由（≤80 字）
         */
        private String reason;

        /**
         * 原文引用（≤60 字，逐字摘自证据）
         */
        private String quote;
    }
}

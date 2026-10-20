package com.his.ai.vo;

import lombok.Data;

/**
 * 预问诊病史摘要出参
 */
@Data
public class PrevisitSummaryVO {

    /**
     * 病史摘要（模型凝练或规则模板）
     */
    private String summary;

    /**
     * 来源：model-模型 rule-规则模板
     */
    private String source;

    /**
     * 是否降级（true=规则模板拼接）
     */
    private Boolean degraded;

    /**
     * 降级原因（模型不可用时给）
     */
    private String degradeReason;
}

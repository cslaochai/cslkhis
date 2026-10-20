package com.his.ai.vo;

import lombok.Data;

/**
 * 随访话术草拟出参
 */
@Data
public class FollowupComposeVO {

    /**
     * 随访话术草稿（医生可改再保存）
     */
    private String content;

    /**
     * 来源：model-模型 rule-类型模板
     */
    private String source;

    /**
     * 是否降级（true=类型模板）
     */
    private Boolean degraded;

    /**
     * 降级原因（模型不可用或文案越界被硬闸拦下时给）
     */
    private String degradeReason;
}

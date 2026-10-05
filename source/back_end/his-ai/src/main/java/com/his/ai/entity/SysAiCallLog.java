package com.his.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.ai.constant.AiCapabilityKeys;
import com.his.ai.enums.AiCallStatusEnum;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 调用审计日志。
 * <p>
 * 医疗场景下这不是「可观测性优化」，是强制要求 —— 病历质控结论要被医务科追溯、
 * 处方审核意见要能应对药事委员会质询，必须能回答「这条结论是哪一版提示词、
 * 哪个模型、什么时候产出的」。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_ai_call_log")
public class SysAiCallLog extends BaseEntity {

    /**
     * 能力标识，见 {@link AiCapabilityKeys}
     */
    private String capabilityKey;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 业务ID
     */
    private Long bizId;

    /**
     * 提供方标识
     */
    private String provider;

    /**
     * 模型名
     */
    private String model;

    /**
     * 提示词版本号
     */
    private String promptVersion;

    /**
     * 输入摘要（已脱敏、已截断）
     */
    private String inputDigest;

    /**
     * 输出摘要（已截断）
     */
    private String outputDigest;

    /**
     * 输入 token 数
     */
    private Integer promptTokens;

    /**
     * 输出 token 数
     */
    private Integer completionTokens;

    /**
     * 调用耗时（毫秒）
     */
    private Integer latencyMs;

    /**
     * 状态，见 {@link AiCallStatusEnum}
     */
    private Integer status;

    /**
     * 失败原因
     */
    private String errorMsg;

    /**
     * 调用人（登录账号）
     */
    private String operator;
}

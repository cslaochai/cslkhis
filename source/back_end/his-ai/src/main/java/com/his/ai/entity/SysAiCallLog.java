package com.his.ai.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.his.ai.constant.AiCapabilityKeys;
import com.his.ai.enums.AiCallStatusEnum;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * AI 调用审计日志。
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

    /**
     * 逻辑删除标志（0 未删除 1 已删除）
     */
    @TableLogic
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}

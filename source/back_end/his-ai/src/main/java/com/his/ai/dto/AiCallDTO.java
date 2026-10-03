package com.his.ai.dto;

import com.his.ai.constant.AiCapabilityKeys;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 一次 AI 能力调用的描述（由业务代码构造）。
 * <p>
 * {@code capabilityKey} 必须来自 {@link AiCapabilityKeys} 的常量，
 * <b>不允许由模型输出来决定</b> —— 这是「工作流」与「Agent」的分界线。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiCallDTO {

    /**
     * 能力标识，决定走哪个开关、哪个超时配置、写哪条审计
     */
    private String capabilityKey;

    /**
     * 提示词模板名（prompts 目录下不含 .md 的文件名）
     */
    private String templateName;

    /**
     * 提示词变量表，占位符写法 {{key}}
     */
    @Builder.Default
    private Map<String, Object> variables = new HashMap<>();

    /**
     * 业务类型，仅用于审计检索
     */
    private String bizType;

    /**
     * 业务ID，仅用于审计检索
     */
    private Long bizId;

    /**
     * 输入摘要（调用方负责脱敏；执行器还会再过一道正则兜底）
     */
    private String inputDigest;

    /**
     * 是否使用轻量模型分流（简单分类类任务用，降低 token 成本）
     */
    @Builder.Default
    private boolean useLiteModel = false;

    /**
     * 输出上限
     */
    @Builder.Default
    private Integer maxTokens = 2048;

    /**
     * 采样温度。结构化抽取统一用低温度减少随机性
     */
    @Builder.Default
    private Double temperature = 0.2D;
}

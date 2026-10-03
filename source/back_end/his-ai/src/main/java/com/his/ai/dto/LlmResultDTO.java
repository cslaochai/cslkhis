package com.his.ai.dto;

import lombok.Data;

/**
 * 模型调用结果
 */
@Data
public class LlmResultDTO {

    /**
     * 模型返回的文本内容。开启 jsonMode 时应为 JSON 字符串。
     */
    private String content;

    /**
     * 实际使用的模型名
     */
    private String model;

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
    private int latencyMs;

    /**
     * 实际尝试次数（含首次）
     */
    private int attempts;
}

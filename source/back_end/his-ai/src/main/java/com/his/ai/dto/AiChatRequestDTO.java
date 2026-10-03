package com.his.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 一次模型调用请求（内部对象，非线上协议体）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequestDTO {

    /**
     * 能力标识，用于日志与审计
     */
    private String capabilityKey;

    /**
     * 模型名，为空时由调用方填入默认模型
     */
    private String model;

    /**
     * 消息列表，第一条通常是 system
     */
    @Builder.Default
    private List<AiMessageDTO> messages = new ArrayList<>();

    /**
     * 采样温度。结构化抽取统一用低温度，减少随机性
     */
    @Builder.Default
    private Double temperature = 0.2D;

    /**
     * 是否要求模型返回严格 JSON（response_format = json_object）
     */
    @Builder.Default
    private boolean jsonMode = true;

    /**
     * 输出上限，防止模型跑飞
     */
    @Builder.Default
    private Integer maxTokens = 2048;
}

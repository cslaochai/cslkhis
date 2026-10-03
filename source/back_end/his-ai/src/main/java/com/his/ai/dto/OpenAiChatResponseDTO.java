package com.his.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * OpenAI 兼容协议的响应体
 * <p>
 * 各厂商会额外返回自己的字段，统一用 ignoreUnknown 忽略，避免因厂商差异解析失败。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAiChatResponseDTO {

    private String id;

    private String model;

    private List<Choice> choices;

    private Usage usage;

    /**
     * 取第一条选择的内容。无内容返回 null。
     */
    public String firstContent() {
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        AiMessageDTO message = choices.get(0).getMessage();
        return message == null ? null : message.getContent();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {

        private Integer index;

        /**
         * 消息内容
         */
        private AiMessageDTO message;

        @JsonProperty("finish_reason")
        private String finishReason;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {

        @JsonProperty("prompt_tokens")
        private Integer promptTokens;

        @JsonProperty("completion_tokens")
        private Integer completionTokens;

        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}

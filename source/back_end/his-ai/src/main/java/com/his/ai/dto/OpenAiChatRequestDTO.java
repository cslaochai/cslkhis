package com.his.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * OpenAI 兼容协议的请求体：POST {baseUrl}/v1/chat/completions
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAiChatRequestDTO {

    private String model;

    private List<AiMessageDTO> messages;

    private Double temperature;

    /**
     * 结构化输出开关。设置后模型被要求只返回 JSON 对象。
     */
    @JsonProperty("response_format")
    private ResponseFormat responseFormat;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    @JsonProperty("stream")
    private Boolean stream;

    @Data
    public static class ResponseFormat {

        /**
         * 固定为 json_object
         */
        private String type;

        public static ResponseFormat jsonObject() {
            ResponseFormat format = new ResponseFormat();
            format.setType("json_object");
            return format;
        }
    }
}

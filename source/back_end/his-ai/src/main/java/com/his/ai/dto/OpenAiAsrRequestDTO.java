package com.his.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * ASR 同步转写请求体（OpenAI 兼容 chat/completions 形态，qwen3-asr-flash 专用）。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAiAsrRequestDTO {

    private String model;

    private List<Message> messages;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Message {
        private String role;
        private List<ContentPart> content;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContentPart {
        /**
         * 固定 input_audio
         */
        private String type;
        @JsonProperty("input_audio")
        private InputAudio inputAudio;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InputAudio {
        /**
         * base64 音频数据（不带 data: 前缀）
         */
        private String data;
        /**
         * 音频格式：wav / mp3 / webm / ogg / opus 等
         */
        private String format;
    }
}

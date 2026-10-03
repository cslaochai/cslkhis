package com.his.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对话消息（OpenAI 兼容协议）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiMessageDTO {

    public static final String ROLE_SYSTEM = "system";
    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";

    /**
     * 角色：system / user / assistant
     */
    private String role;

    /**
     * 内容
     */
    private String content;

    public static AiMessageDTO system(String content) {
        return new AiMessageDTO(ROLE_SYSTEM, content);
    }

    public static AiMessageDTO user(String content) {
        return new AiMessageDTO(ROLE_USER, content);
    }

    public static AiMessageDTO assistant(String content) {
        return new AiMessageDTO(ROLE_ASSISTANT, content);
    }
}

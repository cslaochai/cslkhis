package com.his.ai.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.ai.exception.LlmException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 结构化输出解析器。
 * <p>
 * 模型即使被要求「只输出 JSON」，也常见三种情况需要容错：
 * 用 ```json 代码块包起来、前后加一句解释、字段多返回几个。
 * 这里统一处理，让上层能力代码只面对强类型对象。
 * <p>
 * 注意最后一道闸在能力层：ICD 编码必须校验存在于候选集内。本类只保证「能解析」，
 * 不保证「内容正确」。
 */
@Component
@RequiredArgsConstructor
public class StructuredOutputParser {

    private final ObjectMapper objectMapper;

    /**
     * 从模型返回中抠出 JSON 主体
     */
    public static String extractJson(String raw) {
        if (!StringUtils.hasText(raw)) {
            throw new LlmException("模型返回内容为空");
        }
        String text = raw.trim();

        if (text.startsWith("```")) {
            int firstLineEnd = text.indexOf('\n');
            if (firstLineEnd > 0) {
                text = text.substring(firstLineEnd + 1);
            }
            int lastFence = text.lastIndexOf("```");
            if (lastFence >= 0) {
                text = text.substring(0, lastFence);
            }
            text = text.trim();
        }

        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    /**
     * 解析模型输出为强类型对象
     *
     * @param raw           模型原始返回
     * @param type          目标类型
     * @param capabilityKey 能力标识，仅用于错误信息
     */
    public <T> T parse(String raw, Class<T> type, String capabilityKey) {
        String json = extractJson(raw);
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException ex) {
            throw new LlmException("模型输出不是合法 JSON（" + capabilityKey + "）：" + ex.getOriginalMessage());
        }
    }
}

package com.his.ai.support;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 渲染完成的提示词。
 */
@Data
@AllArgsConstructor
public class RenderedPrompt {

    /**
     * 模板名
     */
    private String name;

    /**
     * 模板版本号，随 AI 调用日志一起落库留痕，用于回溯「哪一版提示词产出了坏结果」
     */
    private String version;

    /**
     * system 段
     */
    private String systemText;

    /**
     * user 段
     */
    private String userText;
}

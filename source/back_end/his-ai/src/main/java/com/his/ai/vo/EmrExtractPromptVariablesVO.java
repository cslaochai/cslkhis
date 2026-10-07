package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 病历文本结构化抽取提示词变量。
 *
 * <p>对应 {@code prompts/emr-extract.md} 的全部占位符。
 * 字段名与模板里的 {{占位符}} 一一对应。
 *
 * <p>{@code fieldCatalog} 是可写字段清单（代码侧的唯一口径），
 * 模型只能按清单里的字段名输出，从源头限制它能编造什么字段。
 */
@Data
public class EmrExtractPromptVariablesVO implements PromptVariables {

    /**
     * 性别
     */
    private String gender;

    /**
     * 年龄（含「岁」后缀，未填写时为「（未填写）」）
     */
    private String age;

    /**
     * 可写字段清单（由 EmrFieldCatalog 生成）
     */
    private String fieldCatalog;

    /**
     * 待抽取的病历原文（已脱敏）
     */
    private String rawText;
}
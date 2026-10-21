package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 病历文本结构化抽取提示词变量。
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
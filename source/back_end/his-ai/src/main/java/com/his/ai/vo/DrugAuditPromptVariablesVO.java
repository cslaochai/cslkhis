package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 处方审核提示词变量。
 *
 * <p>对应 {@code prompts/drug-audit.md} 的全部占位符。
 * 字段名与模板里的 {{占位符}} 一一对应。
 *
 * <p>结构性审查（重复用药、相互作用、禁忌）由代码规则先算出
 * {@code hardRuleHints}，模型只负责补充规则没覆盖的表述层问题。
 */
@Data
public class DrugAuditPromptVariablesVO implements PromptVariables {

    /**
     * 性别（1-男 2-女，取自字典枚举文案）
     */
    private String gender;

    /**
     * 年龄（含「岁」后缀，未填写时为「（未填写）」）
     */
    private String age;

    /**
     * 过敏史，无记录时为「（无已知过敏史记录）」
     */
    private String allergyHistory;

    /**
     * 诊断（临床诊断文本）
     */
    private String diagnosis;

    /**
     * 本次全部处方明细
     */
    private String prescriptions;

    /**
     * 代码硬规则命中的提示行
     */
    private String hardRuleHints;
}
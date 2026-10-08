package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 处方审核提示词变量。
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
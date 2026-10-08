package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 运营问数（结论归纳段）提示词变量。
 */
@Data
public class OperationQaSummaryPromptVariablesVO implements PromptVariables {

    /**
     * 管理者的原始问题
     */
    private String question;

    /**
     * 查询结果的表格文本（结论段只读前 30 行，控制提示词长度）
     */
    private String table;
}
package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 运营问数（结论归纳段）提示词变量。
 *
 * <p>对应 {@code prompts/operation-qa-summary.md} 的全部占位符。
 * 字段名与模板里的 {{占位符}} 一一对应。
 *
 * <p>与 {@link OperationQaPromptVariablesVO} 分开是因为两段用两个模板文件：
 * 换结论话术不必重编译 SQL 生成段的提示词，出问题也能定位是哪一段产生了坏结果。
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
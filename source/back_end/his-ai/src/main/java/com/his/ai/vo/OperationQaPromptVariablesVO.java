package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 运营问数（生成 SQL 段）提示词变量。
 *
 * <p>对应 {@code prompts/operation-qa.md} 的全部占位符。
 * 字段名与模板里的 {{占位符}} 一一对应。
 *
 * <p>{@code schema} 是白名单表的结构说明（表名、字段、常用口径、示例查询），
 * 模型只能依据它写 SELECT；生成的语句还要过 OperationSqlGuard 才能执行。
 */
@Data
public class OperationQaPromptVariablesVO implements PromptVariables {

    /**
     * 白名单表结构说明文本
     */
    private String schema;

    /**
     * 管理者的原始问题
     */
    private String question;

    /**
     * 当天日期（供「今日/本月」这类相对时间落成具体区间）
     */
    private String today;
}
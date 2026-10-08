package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 运营问数（生成 SQL 段）提示词变量。
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
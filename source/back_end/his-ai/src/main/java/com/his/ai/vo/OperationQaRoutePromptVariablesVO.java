package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 运营问数路由兜底（模型选表）提示词变量。
 */
@Data
public class OperationQaRoutePromptVariablesVO implements PromptVariables {

    /**
     * 表目录文本（表名+用途）
     */
    private String catalog;

    /**
     * 管理者的原始问题
     */
    private String question;

    /**
     * 当天日期
     */
    private String today;
}

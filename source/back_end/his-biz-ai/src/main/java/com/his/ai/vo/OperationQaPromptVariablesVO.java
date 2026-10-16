package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 运营问数（生成 SQL 段）提示词变量。
 */
@Data
public class OperationQaPromptVariablesVO implements PromptVariables {

    /**
     * 按需渲染的表结构说明文本（只含问题相关的表）
     */
    private String schema;

    /**
     * 命中的业务口径文本（语义配置注入；无命中时为占位说明）
     */
    private String semantics;

    /**
     * 敏感字段约束文本（脱敏/禁止出库，列级配置注入）
     */
    private String sensitiveRules;

    /**
     * 表间关系文本（权威 join 键，图谱第三层注入；只含与选中表相关的边）
     */
    private String relations;

    /**
     * 管理者的原始问题
     */
    private String question;

    /**
     * 当天日期（供「今日/本月」这类相对时间落成具体区间）
     */
    private String today;
}

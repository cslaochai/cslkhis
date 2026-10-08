package com.his.charge.support;

import java.util.List;

/**
 * 合规审核规则。
 */
public interface ComplianceRule {

    /**
     * 规则分组：A/B/C/D
     */
    String group();

    /**
     * 执行判定
     */
    List<RuleFinding> evaluate(RuleContext context);
}

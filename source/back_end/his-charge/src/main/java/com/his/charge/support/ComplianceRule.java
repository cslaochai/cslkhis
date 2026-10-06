package com.his.charge.support;

import java.util.List;

/**
 * 合规审核规则。
 *
 * <p>实现约定（非常重要）：</p>
 * <ol>
 *   <li><b>必须至少返回一条 finding</b>。判不了就返回 result=3（不适用）并写明原因，
 *       不允许返回空列表 —— 那等于规则静默消失，报告会看起来「没这条问题」。</li>
 *   <li><b>禁止访问数据库</b>，所需数据一律从 {@link RuleContext} 取。</li>
 *   <li><b>禁止用硬编码的权重/标准算分</b>。分组表缺失时返回不适用，不要估算。</li>
 * </ol>
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

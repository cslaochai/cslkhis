package com.his.charge.support;

import java.util.Set;

/**
 * 官方分组规则原文（MDC / ADRG / DRG 三级共用同一套 DSL）编译后的可求值表达式。
 *
 * <p>语法、变量与求值口径见 {@link DrgRuleParser}。
 */
public interface DrgRule {

    /**
     * 按当前病案事实判定该规则是否成立
     */
    boolean matches(DrgFacts facts, DrgScheme scheme);

    /**
     * 规则引用的码集合编号（MCC/CC 两个伪集合除外），供方案装载时做存在性校验。
     */
    Set<String> refs();
}

package com.his.medicaltech.support;

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
}

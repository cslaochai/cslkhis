package com.his.ai.support;

import cn.hutool.core.bean.BeanUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 提示词变量表：一个能力的「本次要填进模板的那些值」。
 */
public interface PromptVariables {

    /**
     * 展平成「占位符名 → 替换文本」的有序表。
     *
     * <p>用 LinkedHashMap 保证顺序与字段声明顺序一致：虽然当前替换逻辑与顺序无关，
     * 但排查问题时「变量顺序 == 提示词里出现顺序」更好读。
     *
     * @return 键为字段名（即占位符名，不含大括号），值为该变量的文本形态；不返回 null
     */
    default Map<String, String> toMap() {
        Map<String, String> text = new LinkedHashMap<>();
        // 字段值为 null 时保留键、值为 null（与原 Map 行为一致）：
        // PromptTemplate 会把 null 渲染成空串，而不是留下 {{占位符}} 原文
        BeanUtil.beanToMap(this).forEach((key, value) -> text.put(key, value == null ? null : value.toString()));
        return text;
    }
}
package com.his.ai.support;

import cn.hutool.core.bean.BeanUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 提示词变量表：一个能力的「本次要填进模板的那些值」。
 *
 * <p><b>为什么不是 {@code Map<String, Object>}</b>：模板占位符（{@code {{patientName}}}）与Java
 * 字段名一一对应，若继续传Map，则「模板里写了占位符、代码里put 漏了」只能等线上提示词里
 * 留下一个 {@code {{patientName}}} 原文才发现 —— 编译器全程沉默。改成 VO 后，
 * 能力实现里是 {@code vo.setPatientName(...)}，IDE 能带着走、用错字段名编译就报错。
 *
 * <p><b>为什么 {@code toMap()} 仍返回 Map、而键名不写死在 VO 里</b>：模板引擎的职责就是
 * 「拿键名去模板里找 {@code {{键名}}} 并替换」，它必须能遍历键集合，这是模板引擎的固有
 * 契约，不是业务数据契约（规约里「真字典保留 Map」指的就是这种）。所以键名由
 * {@link BeanUtil#beanToMap(Object)} 从<b>字段名</b>反射得出，而不是人手写一遍字符串键 ——
 * 人手写等于把 {@code variables.put("patientName", x)} 原地搬进VO，改造等于没做。
 *
 * <p><b>取值类型是 String 而非 Object</b>：模板替换最终一律走字符串（{@code {{totalScore}}}
 * 渲染出来就是 {@code 7}），把 Integer/Boolean 在 VO 里保留成强类型字段、
 * 在 {@link #toMap()} 边界一次性转成文本，语义更清楚，也让模板引擎不必关心值的原始类型。
 *
 * <p><b>字段名即占位符名</b>：新增变量 = 在对应 VO 里加一个字段，模板里写同名
 * {@code {{字段名}}}。若模板里出现了 VO 里没有的占位符，渲染时会被
 * {@link PromptTemplate} 打WARN 日志点出来，不会静默漏填。
 *
 * <p><b>实现类必须带 {@code @Data}</b>：{@link BeanUtil#beanToMap(Object)} 走的是
 * <b>getter</b> 反射，不是字段反射。漏了 {@code @Data}（或漏了某个字段的 getter），
 * 该字段不会出现在 {@link #toMap()} 里，占位符将原样留在提示词里 ——
 * 这与改造前 {@code variables.put} 漏写一样静默，所以实现类已全部标注 {@code @Data}。
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
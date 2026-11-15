package com.his.webfault.vo;

/**
 * 场景目录：面试前先看这张表，知道每个场景「点哪里、看什么现象、用哪条命令」。
 *
 * @param code    场景编码，前端与接口使用的稳定标识
 * @param name    场景名称，直接展示在卡片上
 * @param symptom 预期现象，用来判断故障是否真的发生
 * @param trigger 触发方式，通常是带默认参数的 POST 路径
 * @param observe 排查动作，例如 top + jstack、jmap、EXPLAIN
 */
public record ScenarioVO(
        String code,
        String name,
        String symptom,
        String trigger,
        String observe) {
}

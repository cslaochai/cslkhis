package com.his.faultlab.vo;

/**
 * 场景目录：面试前先看这张表，知道每个场景「点哪里、看什么现象、用哪条命令」。
 */
public record ScenarioVO(
        String code,
        String name,
        String symptom,
        String trigger,
        String observe) {
}

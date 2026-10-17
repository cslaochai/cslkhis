package com.his.ai.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注 LLM 输出 DTO 上<b>允许落审计明文</b>的字段。
 * <p>
 * 纪律 6 的落地口径（G-11）：审计 {@code output_digest} 不落模型输出的临床文本原文。
 * 执行器组装摘要时，<b>标注了本注解的字段过脱敏后明文入摘要</b>（码值、判定结果这类短枚举值，
 * 是 AI 管理台排障的锚点），<b>未标注的 String 字段一律落「字段名=SHA-256 前 12 位」指纹</b> ——
 * 指纹不可逆，但同一段输出两次调用的指纹相同，足以核对「模型是不是回了同一段话」。
 * <p>
 * 标注前自问：这个字段会不会包含患者主诉、病情描述、护理记录这类临床文本？
 * 会 → 不许标。只用于标注规则码、诊断码、verdict、score 这类无隐私的短值。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AiAuditPlain {
}

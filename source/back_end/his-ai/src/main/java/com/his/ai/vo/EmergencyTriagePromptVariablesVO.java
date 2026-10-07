package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 急诊分诊提示词变量。
 *
 * <p>对应 {@code prompts/emergency-triage.md} 的全部占位符。
 * 字段名与模板里的 {{占位符}} 一一对应。
 *
 * <p>{@code currentLevel}/{@code hardLevel} 是代码算出的分级结论，
 * 模型只能在同级别内调整表述，不得跨级（分诊只升不降的纪律由代码兜底）。
 */
@Data
public class EmergencyTriagePromptVariablesVO implements PromptVariables {

    /**
     * 性别
     */
    private String gender;

    /**
     * 年龄（含「岁」后缀，未填写时为「（未填写）」）
     */
    private String age;

    /**
     * 主诉，为空时填「-」
     */
    private String chiefComplaint;

    /**
     * 生命体征描述文本
     */
    private String vitalSigns;

    /**
     * 代码判定的当前分级
     */
    private String currentLevel;

    /**
     * 硬规则兜底分级（只升不降的下限）
     */
    private String hardLevel;

    /**
     * 命中的危急征象红旗清单，无则「（无）」
     */
    private String hardRedFlags;

    /**
     * 关键词渠道判别结果
     */
    private String keywordChannel;
}
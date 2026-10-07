package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 病情恶化预警提示词变量。
 *
 * <p>对应 {@code prompts/deterioration-alert.md} 的全部占位符。
 * 字段名与模板里的 {{占位符}} 一一对应，改提示词加占位符时必须同步加字段。
 *
 * <p>数据全部是「事实层代码算」出来的文本（见 DeteriorationScoreRules），
 * 这里只负责把已算好的事实摆进模板，不做二次判断。
 */
@Data
public class DeteriorationAlertPromptVariablesVO implements PromptVariables {

    /**
     * 患者标签（性别 + 年龄）
     */
    private String patientTag;

    /**
     * 测量时间
     */
    private String measureTime;

    /**
     * 生命体征明细文本
     */
    private String vitalText;

    /**
     * MEWS+SpO2 总分
     */
    private String totalScore;

    /**
     * 预警级（0-未触发 1-关注 2-高危；仅 ≥1 才调模型）
     */
    private String alertLevel;

    /**
     * 预警级中文名（取自字典 biz_ai_deteriorationAlertLevelEnum）
     */
    private String alertText;

    /**
     * 命中的评分项明细
     */
    private String itemsText;
}
package com.his.ai.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 急诊分诊建议出参
 * <p>
 * <b>本对象只承载「建议」，没有「最终级别」的语义。</b>
 * 落库的 {@code triage_level} 永远由分诊护士确认后写入，AI 不写库。
 * <p>
 * {@code currentLevel} 与 {@code suggestedLevel} 同时给出的原因：
 * 分诊护士需要看到「系统建议比我选的更严」这个对比，而不是被系统悄悄改掉分级。
 */
@Data
public class EmergencyTriageResultVO {

    /**
     * 是否降级（模型未参与，仅有硬规则结论）
     */
    private boolean degraded;

    /**
     * 降级原因
     */
    private String degradeReason;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long emergencyId;

    /**
     * 急诊号
     */
    private String emergencyNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 性别文本
     */
    private String genderText;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 主诉（实际用于分析的文本）
     */
    private String chiefComplaint;

    /**
     * 生命体征文本
     */
    private String vitalSignsText;

    /**
     * 是否未采集生命体征。为 true 时必须在 UI 上明示 ——
     * 缺体征时给出的分诊建议可信度显著下降，不能让它看起来和完整输入一样权威。
     */
    private Boolean vitalSignsMissing;

    /**
     * 人工已选分诊级别（未定级时为 null）
     */
    private Integer currentLevel;

    /**
     * 人工已选级别文本
     */
    private String currentLevelText;

    /**
     * 人工已选区域
     */
    private String currentZone;

    /**
     * 人工已选绿色通道
     */
    private String currentGreenChannel;

    /**
     * 硬规则给出的最低级别（无红旗时为 null）
     */
    private Integer hardLevel;

    /**
     * 硬规则级别文本
     */
    private String hardLevelText;

    /**
     * 命中的红旗征象
     */
    private List<String> redFlags = new ArrayList<>();

    /**
     * 建议级别（只升不降后的最终建议值）
     */
    private Integer suggestedLevel;

    /**
     * 建议级别文本
     */
    private String suggestedLevelText;

    /**
     * 建议区域（由建议级别推导，不由模型给出）
     */
    private String suggestedZone;

    /**
     * 建议绿色通道
     */
    private String suggestedGreenChannel;

    /**
     * 建议级别的来源：HARD_RULE / LLM / MANUAL / NONE
     */
    private String levelBasis;

    /**
     * 是否建议升级（建议级别比人工已选更严重）
     */
    private Boolean upgradeRecommended;

    /**
     * 判断依据
     */
    private String reasoning;

    /**
     * 建议立即采取的措施
     */
    private List<String> recommendActions = new ArrayList<>();

    /**
     * 给分诊护士的提示语：明确说明「本建议不改变你已选的分级」
     */
    private String tip;

    /**
     * 耗时（毫秒）
     */
    private Long latencyMs;
}

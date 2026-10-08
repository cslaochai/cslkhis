package com.his.operation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 麻醉术前访视单保存入参（新增 / 修改草稿）。
 */
@Data
public class AnesthesiaVisitUpsertDTO implements Serializable {

    /**
     * 为空 = 新增；非空 = 修改（仅草稿可改）
     */
    private Long id;

    /**
     * 手术申请单ID
     */
    @NotNull(message = "手术申请单ID不能为空（访视必须挂在具体一台手术上）")
    private Long applyId;

    /**
     * ASA 分级（1-Ⅰ 2-Ⅱ 3-Ⅲ 4-Ⅳ 5-Ⅴ）
     */
    @Min(value = 1, message = "ASA 分级取值不合法（应为 1~5）")
    @Max(value = 5, message = "ASA 分级取值不合法（应为 1~5）")
    private Integer asaGrade;

    /**
     * ASA E（急诊）标志：0-否 1-是
     */
    private Integer asaEmergency;

    /**
     * Mallampati 气道分级（1-Ⅰ 2-Ⅱ 3-Ⅲ 4-Ⅳ）
     */
    @Min(value = 1, message = "Mallampati 分级取值不合法（应为 1~4）")
    @Max(value = 4, message = "Mallampati 分级取值不合法（应为 1~4）")
    private Integer mallampati;

    /**
     * 张口度（cm）
     */
    private BigDecimal mouthOpenCm;

    /**
     * 颈部活动度（1-正常 2-受限 3-强直）
     */
    private Integer neckMobility;

    /**
     * 预计困难气道（0-否 1-是）
     */
    private Integer difficultAirway;

    /**
     * 气道评估补充说明
     */
    private String airwayNote;

    /**
     * 既往麻醉史与不良反应史
     */
    private String pastAnesthesiaHistory;

    /**
     * 过敏史（药物/食物/消毒剂）
     */
    private String allergyHistory;

    /**
     * 长期用药史（抗凝药/降压药/激素等必须写）
     */
    private String medicationHistory;

    /**
     * 吸烟饮酒史
     */
    private String smokeDrink;

    /**
     * 禁食禁饮（0-未禁食 1-已按要求禁食 2-急诊饱胃）
     */
    @Min(value = 0, message = "禁食禁饮状态取值不合法（应为 0~2）")
    @Max(value = 2, message = "禁食禁饮状态取值不合法（应为 0~2）")
    private Integer npoStatus;

    /**
     * 身高（cm）
     */
    private BigDecimal heightCm;

    /**
     * 体重（kg）
     */
    private BigDecimal weightKg;

    /**
     * 辅助检查摘要（血常规/凝血/ECG/胸片/电解质等）
     */
    private String examSummary;

    /**
     * 麻醉计划
     */
    private String anesthesiaPlan;

    /**
     * 监测计划（有创血压/CVP/BIS/体温等）
     */
    private String monitoringPlan;

    /**
     * 风险评估
     */
    private String riskAssessment;

    /**
     * 备选方案
     */
    private String backupPlan;

    /**
     * 访视结论：1-可施行麻醉 2-暂缓手术 3-需会诊/进一步评估（完成访视时必填）
     */
    @Min(value = 1, message = "访视结论取值不合法（应为 1-可施行麻醉 / 2-暂缓手术 / 3-需会诊）")
    @Max(value = 3, message = "访视结论取值不合法（应为 1-可施行麻醉 / 2-暂缓手术 / 3-需会诊）")
    private Integer conclusion;

    /**
     * 结论说明
     */
    private String conclusionNote;

    /**
     * 备注
     */
    private String remark;
}

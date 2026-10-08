package com.his.patient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 住院病历文书新增 / 修改入参（命名遵循 AGENTS.md：增改用 `xxxUpsertDTO`）。
 */
@Data
public class InpatientRecordUpsertDTO implements Serializable {

    /**
     * 文书ID（为空 = 新增）
     */
    private Long id;

    /**
     * 入院ID（新增必填）
     */
    private Long admissionId;

    /**
     * 文书类型：1-入院记录 2-首次病程 3-日常病程 4-术前小结 5-手术记录 6-术后首次病程 7-出院记录 8-死亡记录
     */
    @Schema(description = "文书类型：1-入院记录 2-首次病程 3-日常病程 4-术前小结 5-手术记录 6-术后首次病程 7-出院记录 8-死亡记录")
    @Min(value = 1, message = "文书类型取值不合法（应为 1~10）")
    @Max(value = 10, message = "文书类型取值不合法（应为 1~10）")
    private Integer recordType;

    /**
     * 文书标题（为空时取类型文案）
     */
    private String recordTitle;

    /**
     * 记录时间（为空时取当前时间）
     */
    private LocalDateTime recordTime;

    // 病史

    /**
     * 主诉
     */
    private String chiefComplaint;

    /**
     * 现病史
     */
    private String presentIllness;

    /**
     * 既往史
     */
    private String pastHistory;

    /** 个人史（含婚育、烟酒、职业） */
    private String personalHistory;

    /**
     * 家族史
     */
    private String familyHistory;

    /**
     * 过敏史
     */
    private String allergyHistory;

    // 生命体征

    /**
     * 体温（℃）
     */
    private BigDecimal temperature;

    /**
     * 脉搏（次/分）
     */
    private Integer pulse;

    /**
     * 呼吸（次/分）
     */
    private Integer respiration;

    /**
     * 收缩压（mmHg）
     */
    private Integer systolicPressure;

    /**
     * 舒张压（mmHg）
     */
    private Integer diastolicPressure;

    /**
     * 身高（cm）
     */
    private BigDecimal height;

    /**
     * 体重（kg）
     */
    private BigDecimal weight;

    // 体格检查

    /** 一般情况（神志/发育/营养/体位/面容） */
    private String generalCondition;

    /**
     * 皮肤黏膜
     */
    private String skinMucosa;

    /**
     * 头颈部
     */
    private String headNeck;

    /**
     * 胸部及肺
     */
    private String chestLung;

    /**
     * 心脏
     */
    private String heart;

    /**
     * 腹部
     */
    private String abdomen;

    /**
     * 脊柱四肢
     */
    private String spineLimbs;

    /**
     * 神经系统
     */
    private String nervousSystem;

    /**
     * 专科检查
     */
    private String specialistExam;

    // 结论

    /**
     * 辅助检查
     */
    private String auxiliaryExam;

    /**
     * 诊断名称
     */
    private String diagnosisName;

    /**
     * 诊断编码
     */
    private String diagnosisCode;

    /** 诊疗计划 / 处理意见 */
    private String treatmentPlan;

    /**
     * 病程正文（病程类文书用）
     */
    private String courseNote;

    /**
     * 备注
     */
    private String remark;
}

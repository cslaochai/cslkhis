package com.his.charge.vo;

import com.his.charge.api.EmrGateway;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 病历记录跨域摘要：清单与病历的诊断一致性比对只读这 6 个字段。
 */
@Data
@NoArgsConstructor
public class MedicalRecordBriefVO {

    /**
     * 病历号（比对失败时提示医生"哪一份病历"）
     */
    private String recordNo;

    /**
     * 诊断编码（与清单主诊断的 ICD-10 比对）
     */
    private String diagnosisCode;

    /**
     * 诊断名称
     */
    private String diagnosisName;

    /**
     * 诊断文本（未编码时的兜底比对）
     */
    private String diagnosis;

    /**
     * 患者性别（逻辑排他规则要用）
     */
    private Integer gender;

    /**
     * 患者年龄
     */
    private Integer age;

    /**
     * 主诉（现病史分析的素材）
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

    /**
     * 专科检查（体格/专科检查所见）
     */
    private String specialistExam;

    /**
     * 辅助检查（检验检查结果的引用文本）
     */
    private String auxiliaryExam;

    /**
     * 处理计划/医嘱（治疗合理性判定的素材）
     */
    private String treatmentPlan;
}

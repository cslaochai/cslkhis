package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 病历正文与体征快照：跨域做文本生成、内涵质控与临床取值时要用的那几列。
 */
@Data
@NoArgsConstructor
public class AiMedicalRecordContentVO {

    /**
     * 病历ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 性别（1-男 2-女 9-未知）
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

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

    /**
     * 个人史
     */
    private String personalHistory;

    /**
     * 家族史
     */
    private String familyHistory;

    /**
     * 过敏史
     */
    private String allergyHistory;

    /**
     * 一般状况
     */
    private String generalCondition;

    /**
     * 皮肤与黏膜
     */
    private String skinMucosa;

    /**
     * 头颈部检查
     */
    private String headNeck;

    /**
     * 胸部与肺检查
     */
    private String chestLung;

    /**
     * 心脏检查
     */
    private String heart;

    /**
     * 腹部检查
     */
    private String abdomen;

    /**
     * 脊柱与四肢检查
     */
    private String spineLimbs;

    /**
     * 神经系统检查
     */
    private String nervousSystem;

    /**
     * 专科检查
     */
    private String specialistExam;

    /**
     * 辅助检查
     */
    private String auxiliaryExam;

    /**
     * 体温
     */
    private String temperature;

    /**
     * 脉搏
     */
    private String pulse;

    /**
     * 呼吸
     */
    private String respiration;

    /**
     * 收缩压
     */
    private String systolicPressure;

    /**
     * 舒张压
     */
    private String diastolicPressure;

    /**
     * 诊断
     */
    private String diagnosis;

    /**
     * 诊断名称
     */
    private String diagnosisName;

    /**
     * 处理意见
     */
    private String treatmentPlan;
}

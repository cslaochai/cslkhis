package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 患者端门诊病历（列表只填头部字段，详情再填正文）。
 */
@Data
public class MyMedicalRecordVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 病历号
     */
    private String recordNo;

    /**
     * 就诊日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    /**
     * 就诊类型（1-初诊 2-复诊）
     */
    private Integer visitType;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 主诉
     */
    private String chiefComplaint;

    /**
     * 诊断
     */
    private String diagnosis;

    /**
     * 病历状态（1-草稿 2-已提交 3-已归档 4-已作废）
     */
    private Integer recordStatus;

    private String recordStatusText;

    // 以下仅详情返回

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
     * 体温（℃）
     */
    private String temperature;

    /**
     * 脉搏（次/分）
     */
    private String pulse;

    /**
     * 呼吸（次/分）
     */
    private String respiration;

    /**
     * 收缩压（mmHg）
     */
    private String systolicPressure;

    /**
     * 舒张压（mmHg）
     */
    private String diastolicPressure;

    /**
     * 一般情况
     */
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
     * 胸肺
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

    /**
     * 辅助检查
     */
    private String auxiliaryExam;

    /**
     * 处理意见
     */
    private String treatmentPlan;

    /**
     * 提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;
}

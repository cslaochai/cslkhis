package com.his.emr.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 病历保存入参（临时保存 / 结诊共用）
 */
@Data
public class MedicalRecordSaveDTO {

    // 病历信息

    /**
     * 病历ID，编辑已有病历时必填；新建时为空
     */
    private Long recordId;

    // 患者信息

    /**
     * 患者ID
     */
    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    /**
     * 患者号（患者唯一编号）
     */
    @NotBlank(message = "患者号不能为空")
    private String patientNo;

    /**
     * 患者姓名
     */
    @NotBlank(message = "患者姓名不能为空")
    private String patientName;

    /**
     * 挂号ID（本次就诊对应的挂号单）
     */
    @NotNull(message = "挂号ID不能为空")
    private Long registId;

    /**
     * 排队叫号ID（本次就诊对应的队列记录）
     */
    @NotNull(message = "队列ID不能为空")
    private Long queueId;

    // 科室医生信息

    /**
     * 接诊科室ID
     */
    @NotNull(message = "科室ID不能为空")
    private Long deptId;

    /**
     * 接诊科室名称
     */
    @NotBlank(message = "科室名称不能为空")
    private String deptName;

    /**
     * 接诊医生ID
     */
    @NotNull(message = "医生ID不能为空")
    private Long doctorId;

    /**
     * 接诊医生姓名
     */
    @NotBlank(message = "医生姓名不能为空")
    private String doctorName;

    // 病历正文

    /**
     * 主诉
     */
    @NotBlank(message = "主诉不能为空")
    private String chiefComplaint;

    /**
     * 现病史
     */
    @NotBlank(message = "现病史不能为空")
    private String presentIllness;

    /**
     * 过敏史
     */
    @NotBlank(message = "过敏史不能为空")
    private String allergyHistory;

    /**
     * 既往史
     */
    @NotBlank(message = "既往史不能为空")
    private String pastHistory;

    /**
     * 个人史
     */
    @NotBlank(message = "个人史不能为空")
    private String personalHistory;

    /**
     * 家族史
     */
    @NotBlank(message = "家族史不能为空")
    private String familyHistory;

    /**
     * 体温，单位：℃
     */
    private String temperature;

    /**
     * 脉搏，单位：次/分
     */
    private String pulse;

    /**
     * 呼吸，单位：次/分
     */
    private String respiration;

    /**
     * 收缩压，单位：mmHg
     */
    private String systolicPressure;

    /**
     * 舒张压，单位：mmHg
     */
    private String diastolicPressure;

    /**
     * 一般情况（体格检查）
     */
    private String generalCondition;

    /**
     * 皮肤黏膜（体格检查）
     */
    private String skinMucosa;

    /**
     * 头颈部（体格检查）
     */
    private String headNeck;

    /**
     * 胸肺（体格检查）
     */
    private String chestLung;

    /**
     * 心脏（体格检查）
     */
    private String heart;

    /**
     * 腹部（体格检查）
     */
    private String abdomen;

    /**
     * 脊柱四肢（体格检查）
     */
    private String spineLimbs;

    /**
     * 神经系统（体格检查）
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
     * 诊断（诊断结论原文）
     */
    @NotBlank(message = "诊断不能为空")
    private String diagnosis;

    /**
     * 诊断编码（ICD-10 编码）
     */
    private String diagnosisCode;

    /**
     * 诊断名称（ICD-10 名称）
     */
    private String diagnosisName;

    /**
     * 处理意见（治疗方案）
     */
    private String treatmentPlan;

    /**
     * AI 草稿原文（G-10 留痕）：医生点「填入草稿」时前端记住的现病史草稿原文。
     * 保存时携带，后端与终稿做 diff 落留痕表；未用草稿不传，保存后前端即清空。
     */
    private String aiDraftPresentIllness;

    // 处方信息

    /**
     * 处方列表（本次就诊开具的全部处方）
     */
    @Valid
    private List<PrescriptionDTO> prescriptions;

    // 检查申请

    /**
     * 检查申请列表（本次就诊开具的全部检查申请）
     */
    @Valid
    private List<InspectionApplyDTO> inspectionApplies;

    // 检验申请

    /**
     * 检验申请列表（本次就诊开具的全部检验申请）
     */
    @Valid
    private List<LaboratoryApplyDTO> laboratoryApplies;

    // 操作类型

    /**
     * 操作类型：1-临时保存 2-结诊
     */
    private Integer operationType;

    /**
     * 处方入参
     */
    @Data
    public static class PrescriptionDTO {

        /**
         * 处方ID，编辑已有处方时必填；新建时为空
         */
        private Long id;

        /**
         * 处方类型：1-西药处方 2-中成药处方 3-中药饮片处方
         */
        private Integer prescriptionType;

        /**
         * 慢病长处方（M1）：true 时服务端校验患者存在已认定的慢病档案，
         * 且用药天数 ≤ 90；不满足直接拒绝开方。
         */
        private Boolean isLongPrescription;

        /**
         * 长处方用药天数（1~90），isLongPrescription=true 时必填
         */
        private Integer longPrescriptionDays;

        /**
         * 中药饮片剂数（1~30），处方类型=3 必填（sql/139）
         */
        private Integer doseCount;

        /**
         * 中药煎服方式：1-代煎 2-自煎（字典 his_tcm_decoct_flag），处方类型=3 必填
         */
        private Integer decoctFlag;

        /**
         * 处方明细列表
         */
        @Valid
        private List<PrescriptionDetailDTO> details;
    }

    /**
     * 处方明细入参
     */
    @Data
    public static class PrescriptionDetailDTO {

        /**
         * 处方明细ID，编辑已有明细时必填；新建时为空
         */
        private Long id;

        /**
         * 药品ID
         */
        @NotNull(message = "药品ID不能为空")
        private Long drugId;

        /**
         * 药品名称
         */
        private String drugName;

        /**
         * 规格（如 0.25g*24粒）
         */
        private String specification;

        /**
         * 单位（如盒、支、片）
         */
        private String unit;

        /**
         * 单次用量（如 1片、10ml）
         */
        @NotBlank(message = "单次用量不能为空")
        private String singleDosage;

        /**
         * 用药频次（如一日三次）
         */
        @NotBlank(message = "频次不能为空")
        private String frequency;

        /**
         * 给药途径（如口服、静脉滴注）
         */
        @NotBlank(message = "给药途径不能为空")
        private String route;

        /**
         * 数量（开药总量）
         */
        @NotNull(message = "数量不能为空")
        private BigDecimal quantity;

        /**
         * 用药天数
         */
        @NotNull(message = "天数不能为空")
        private Integer duration;

        /**
         * 备注（用药说明）
         */
        private String remark;
    }

    /**
     * 检查申请入参
     */
    @Data
    public static class InspectionApplyDTO {

        /**
         * 检查申请ID，编辑已有申请时必填；新建时为空
         */
        private Long id;

        /**
         * 检查项目ID
         */
        @NotNull(message = "检查项目ID不能为空")
        private Long itemId;

        /**
         * 检查部位
         */
        private String bodyPart;

        /**
         * 检查目的
         */
        private String purpose;

        /**
         * 临床诊断
         */
        private String clinicalDiagnosis;

        /**
         * 是否急诊：0-否 1-是
         */
        private Integer isEmergency;
    }

    /**
     * 检验申请入参
     */
    @Data
    public static class LaboratoryApplyDTO {

        /**
         * 检验申请ID，编辑已有申请时必填；新建时为空
         */
        private Long id;

        /**
         * 检验项目ID
         */
        @NotNull(message = "检验项目ID不能为空")
        private Long itemId;

        /**
         * 标本类型（如静脉血、尿液）
         */
        private String specimenType;

        /**
         * 检验目的
         */
        private String purpose;

        /**
         * 临床诊断
         */
        private String clinicalDiagnosis;

        /**
         * 是否空腹：0-否 1-是
         */
        private Integer isFasting;

        /**
         * 是否急诊：0-否 1-是
         */
        private Integer isEmergency;
    }
}

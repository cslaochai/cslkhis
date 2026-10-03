package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 心电工作台详情（sql/173）：检查记录 + 波形（含采样 JSON）+ 测量参数 + Holter 分析 + 报告。
 *
 * <p>{@code waveData} 是 12 导联采样 JSON 字符串（约 200KB/次），前端 JSON.parse 后
 * 交给 EcgWavePanel 渲染；这里不再二次包装成对象 —— 波形结构属于设备对接契约，
 * 前端渲染层直接消费原始 JSON，中间不转换。
 */
@Data
public class EcgDetailVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    private String recordNo;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    private String applyNo;

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 患者编号 */
    private String patientNo;

    /** 患者姓名 */
    private String patientName;

    /** 性别（1-男 2-女 9-未知） */
    private Integer gender;

    private String genderText;

    /** 年龄 */
    private Integer age;

    /** 就诊日期 */
    private LocalDate visitDate;

    private String itemCode;

    /** 项目名称 */
    private String itemName;

    private String bodyPart;

    private String applyDeptName;

    /** 申请医生 */
    private String applyDoctorName;

    private String clinicalDiagnosis;

    private Integer recordStatus;

    private String recordStatusText;

    private LocalDateTime checkInTime;

    private String executeBy;

    private LocalDateTime executeTime;

    // 波形

    @JsonSerialize(using = ToStringSerializer.class)
    private Long waveId;

    private String waveNo;

    private Integer ecgType;

    private String ecgTypeText;

    private String deviceNo;

    private String collectBy;

    private LocalDateTime collectTime;

    /** 12 导联采样 JSON 字符串（EcgWavePanel 消费） */
    private String waveData;

    // 测量参数

    @JsonSerialize(using = ToStringSerializer.class)
    private Long measureId;

    private Integer hr;

    private Integer prMs;

    private Integer qrsMs;

    private Integer qtMs;

    private Integer qtcMs;

    private Integer pAxis;

    private Integer qrsAxis;

    private Integer tAxis;

    private String rhythmText;

    private String measureBy;

    private LocalDateTime measureTime;

    // Holter 分析

    @JsonSerialize(using = ToStringSerializer.class)
    private Long holterId;

    private LocalDateTime wearStartTime;

    private LocalDateTime wearEndTime;

    private Integer totalBeats;

    private Integer avgHr;

    private Integer maxHr;

    private String maxHrTime;

    private Integer minHr;

    private String minHrTime;

    private Integer afibFlag;

    private Integer afibBeats;

    private Integer svcCount;

    private Integer pvcCount;

    private Integer vtCount;

    private Integer pauseCount;

    private Integer longestPauseMs;

    private Integer stEpisodeCount;

    /** 24小时逐时平均心率（JSON 数组字符串） */
    private String hourlyHrJson;

    private String analysisBy;

    private LocalDateTime analysisTime;

    // 报告（reportId == null = 未写）

    @JsonSerialize(using = ToStringSerializer.class)
    private Long reportId;

    private String reportNo;

    private Integer reportStatus;

    private String reportStatusText;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

    private String reportContent;

    /** 结论 */
    private String conclusion;

    private String suggestions;

    private Integer positiveFlag;

    private String positiveFlagText;

    private Integer isCritical;

    private String writeBy;

    private LocalDateTime writeTime;

    private String auditBy;

    /** 审核时间 */
    private LocalDateTime auditTime;

    private String publishBy;

    private LocalDateTime publishTime;

    private Integer reportVersion;

    private String rejectReason;

    /** 报告医师签名ID（null=未签；双签留痕的另一半在审核签名上） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reportSignId;

    private LocalDateTime reportSignedTime;

    /** 审核医师签名ID（null=未审核） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long auditSignId;

    private LocalDateTime auditSignedTime;
}

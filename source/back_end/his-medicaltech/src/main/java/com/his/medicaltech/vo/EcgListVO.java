package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 心电工作台列表行（sql/173）。
 */
@Data
public class EcgListVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    private String recordNo;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    private String applyNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者编号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 性别（1-男 2-女 9-未知）
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    private String itemCode;

    /**
     * 项目名称
     */
    private String itemName;

    private String bodyPart;

    /**
     * 检查记录状态（1-已登记 2-已签到 3-检查中 4-已出结果 5-已审核 6-已发布 7-已取消）
     */
    private Integer recordStatus;

    private String recordStatusText;

    private String applyDeptName;

    /**
     * 申请医生
     */
    private String applyDoctorName;

    private String clinicalDiagnosis;

    /**
     * 就诊日期
     */
    private LocalDate visitDate;

    // 波形侧（waveId == null = 待采集）

    @JsonSerialize(using = ToStringSerializer.class)
    private Long waveId;

    /**
     * 心电类型（字典 his_ecg_type：1-常规 2-Holter动态；null=还没采集）
     */
    private Integer ecgType;

    private String ecgTypeText;

    private String collectBy;

    private LocalDateTime collectTime;

    /**
     * 测量参数是否已录（心电测量参数的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long measureId;

    // Holter 侧（holterId == null = 未分析）

    @JsonSerialize(using = ToStringSerializer.class)
    private Long holterId;

    private LocalDateTime wearStartTime;

    private LocalDateTime wearEndTime;

    private Integer totalBeats;

    private Integer avgHr;

    private Integer maxHr;

    private Integer minHr;

    /**
     * 是否检出房颤（0/1）
     */
    private Integer afibFlag;

    private Integer pvcCount;

    private Integer svcCount;

    private Integer longestPauseMs;

    // 报告侧（reportId == null = 待书写）

    @JsonSerialize(using = ToStringSerializer.class)
    private Long reportId;

    private String reportNo;

    /**
     * 报告状态（null-未写报告 0-草稿 1-待审核 3-已审核 4-已发布 5-已作废）
     */
    private Integer reportStatus;

    private String reportStatusText;

    /**
     * 阴阳性（字典 his_positive_flag）
     */
    private Integer positiveFlag;

    private String positiveFlagText;

    /**
     * 是否危急（0/1）
     */
    private Integer isCritical;

    private String writeBy;

    private LocalDateTime writeTime;

    private String auditBy;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    private String publishBy;

    private LocalDateTime publishTime;

    /**
     * 报告版本号（被退回重写过几次看这个）
     */
    private Integer reportVersion;

    private String rejectReason;

    /**
     * 报告签名ID（null=未签）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reportSignId;

    /**
     * 审核签名ID（null=未签）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long auditSignId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

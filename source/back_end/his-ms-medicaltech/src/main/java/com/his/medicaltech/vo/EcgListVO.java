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

    /** 检查/检验记录ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /** 检查记录号 */
    private String recordNo;

    /** 检查申请ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /** 检查申请单号 */
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

    /** 检查项目编码 */
    private String itemCode;

    /**
     * 项目名称
     */
    private String itemName;

    /** 检查部位 */
    private String bodyPart;

    /**
     * 检查记录状态（1-已登记 2-已签到 3-检查中 4-已出结果 5-已审核 6-已发布 7-已取消）
     */
    private Integer recordStatus;

    /** 检查记录状态文案（由字典渲染） */
    private String recordStatusText;

    /** 申请科室 */
    private String applyDeptName;

    /**
     * 申请医生
     */
    private String applyDoctorName;

    /** 临床诊断 */
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

    /** 心电类型文案（由字典渲染） */
    private String ecgTypeText;

    /** 采集人 */
    private String collectBy;

    /** 采集时间 */
    private LocalDateTime collectTime;

    /**
     * 测量参数是否已录（心电测量参数的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long measureId;

    // Holter 侧（holterId == null = 未分析）

    @JsonSerialize(using = ToStringSerializer.class)
    private Long holterId;

    /** 开始佩戴时间 */
    private LocalDateTime wearStartTime;

    /** 结束佩戴时间 */
    private LocalDateTime wearEndTime;

    /** 总心搏数（24h 正常约 8~14 万） */
    private Integer totalBeats;

    /** 平均心率（次/分） */
    private Integer avgHr;

    /** 最快心率（次/分） */
    private Integer maxHr;

    /** 最慢心率（次/分） */
    private Integer minHr;

    /**
     * 是否检出房颤（0/1）
     */
    private Integer afibFlag;

    /** 室性早搏总数 */
    private Integer pvcCount;

    /** 室上性早搏总数 */
    private Integer svcCount;

    /** 最长停搏时长（ms；>2000 为临床关注点） */
    private Integer longestPauseMs;

    // 报告侧（reportId == null = 待书写）

    @JsonSerialize(using = ToStringSerializer.class)
    private Long reportId;

    /** 报告编号 */
    private String reportNo;

    /**
     * 报告状态（null-未写报告 0-草稿 1-待审核 3-已审核 4-已发布 5-已作废）
     */
    private Integer reportStatus;

    /** 报告状态文案（由字典渲染，未写报告时给"未写报告"） */
    private String reportStatusText;

    /**
     * 阴阳性（字典 his_positive_flag）
     */
    private Integer positiveFlag;

    /** 阴阳性文案（由字典渲染） */
    private String positiveFlagText;

    /**
     * 是否危急（0/1）
     */
    private Integer isCritical;

    /** 报告书写人 */
    private String writeBy;

    /** 报告书写时间 */
    private LocalDateTime writeTime;

    /** 审核人（初审） */
    private String auditBy;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /** 发布人 */
    private String publishBy;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /**
     * 报告版本号（被退回重写过几次看这个）
     */
    private Integer reportVersion;

    /** 退回原因（审核不通过时必填） */
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

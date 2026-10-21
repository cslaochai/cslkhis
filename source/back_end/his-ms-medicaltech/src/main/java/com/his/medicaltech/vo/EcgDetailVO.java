package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 心电工作台详情（sql/173）：检查记录 + 波形（含采样 JSON）+ 测量参数 + Holter 分析 + 报告。
 */
@Data
public class EcgDetailVO {

    /** 检查/检验记录ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /** 检查/检验记录号 */
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

    /** 性别文案 */
    private String genderText;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 就诊日期
     */
    private LocalDate visitDate;

    /** 检查项目编码 */
    private String itemCode;

    /**
     * 项目名称
     */
    private String itemName;

    /** 检查部位 */
    private String bodyPart;

    /** 申请科室 */
    private String applyDeptName;

    /**
     * 申请医生
     */
    private String applyDoctorName;

    /** 临床诊断 */
    private String clinicalDiagnosis;

    /** 检查记录状态（1-已登记 2-已签到 3-检查中 4-已出结果 5-已审核 6-已发布 7-已取消） */
    private Integer recordStatus;

    /** 检查记录状态文案（由字典渲染） */
    private String recordStatusText;

    /** 签到时间 */
    private LocalDateTime checkInTime;

    /** 执行人 */
    private String executeBy;

    /** 执行时间 */
    private LocalDateTime executeTime;

    // 波形

    @JsonSerialize(using = ToStringSerializer.class)
    private Long waveId;

    /** 波形号 */
    private String waveNo;

    /** 心电类型（1-常规静息心电图 2-24小时动态心电图） */
    private Integer ecgType;

    /** 心电类型文案（由字典渲染） */
    private String ecgTypeText;

    /** 采集设备号（模拟采集固定为 SIM） */
    private String deviceNo;

    /** 采集人 */
    private String collectBy;

    /** 采集时间 */
    private LocalDateTime collectTime;

    /**
     * 12 导联采样 JSON 字符串（EcgWavePanel 消费）
     */
    private String waveData;

    // 测量参数

    @JsonSerialize(using = ToStringSerializer.class)
    private Long measureId;

    /** 心率（次/分） */
    private Integer hr;

    /** PR间期（ms，正常 120~200） */
    private Integer prMs;

    /** QRS时限（ms，正常 60~100） */
    private Integer qrsMs;

    /** QT间期（ms） */
    private Integer qtMs;

    /** QTc校正间期（ms） */
    private Integer qtcMs;

    /** P电轴（°） */
    private Integer pAxis;

    /** QRS电轴（°，正常 -30~+90） */
    private Integer qrsAxis;

    /** T电轴（°） */
    private Integer tAxis;

    /** 节律描述（如窦性心律/心房颤动，非码值翻译） */
    private String rhythmText;

    /** 测量人 */
    private String measureBy;

    /** 测量时间 */
    private LocalDateTime measureTime;

    // Holter 分析

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

    /** 最快心率时刻（HH:mm） */
    private String maxHrTime;

    /** 最慢心率（次/分） */
    private Integer minHr;

    /** 最慢心率时刻（HH:mm） */
    private String minHrTime;

    /** 是否检出房颤（0-否 1-是） */
    private Integer afibFlag;

    /** 房颤心搏数 */
    private Integer afibBeats;

    /** 室上性早搏总数 */
    private Integer svcCount;

    /** 室性早搏总数 */
    private Integer pvcCount;

    /** 室性心动过速阵数 */
    private Integer vtCount;

    /** 停搏（长间歇）次数 */
    private Integer pauseCount;

    /** 最长停搏时长（ms；>2000 为临床关注点） */
    private Integer longestPauseMs;

    /** ST段异常发作阵数 */
    private Integer stEpisodeCount;

    /**
     * 24小时逐时平均心率（JSON 数组字符串）
     */
    private String hourlyHrJson;

    /** 分析人 */
    private String analysisBy;

    /** 分析时间 */
    private LocalDateTime analysisTime;

    // 报告（reportId == null = 未写）

    @JsonSerialize(using = ToStringSerializer.class)
    private Long reportId;

    /** 报告编号 */
    private String reportNo;

    /** 报告状态（0-草稿 1-待审核 2-初审通过 3-已审核 4-已发布 5-已作废） */
    private Integer reportStatus;

    /** 报告状态文案（由字典渲染） */
    private String reportStatusText;

    /** 使用的报告模板ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

    /** 报告内容 */
    private String reportContent;

    /**
     * 结论
     */
    private String conclusion;

    /** 建议 */
    private String suggestions;

    /** 阴阳性（0-未判定 1-阴性 2-阳性 3-未见异常） */
    private Integer positiveFlag;

    /** 阴阳性文案（由字典渲染） */
    private String positiveFlagText;

    /** 是否危急（0-否 1-是，由诊断医师写报告时判定） */
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

    /** 报告版本号（被退回重写过几次看这个） */
    private Integer reportVersion;

    /** 退回原因（审核不通过时必填） */
    private String rejectReason;

    /**
     * 报告医师签名ID（null=未签；双签留痕的另一半在审核签名上）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reportSignId;

    /** 报告签名时刻 */
    private LocalDateTime reportSignedTime;

    /**
     * 审核医师签名ID（null=未审核）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long auditSignId;

    /** 审核签名时刻 */
    private LocalDateTime auditSignedTime;
}

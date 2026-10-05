package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 死亡证明出参集合。
 *
 * <p>脱敏口径（AGENTS.md 第 5 条）：{@link Row} 是列表/台账用的展示行，<b>不选身份证列</b>
 * （台账渲染的是姓名/性别/年龄/死因，页面不渲染的敏感列不出参）；
 * {@link Detail} 是编辑回显（前端 {@code Object.assign(form, res.data)} 后整对象回写 upsert），
 * 必须保持明文，否则一次保存就把证明上的身份证号洗成星号。
 */
public class DeathCertificateVO {

    /**
     * 台账行（含时限派生值 overdue/remainHours，由服务端算，前端不许自己比时间）
     */
    @Data
    public static class Row {
        /**
         * 主键（雪花ID）
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        /**
         * 证明编号
         */
        private String certNo;

        /**
         * 住院记录ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long admissionId;

        /**
         * 死亡出院记录ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long dischargeId;

        /**
         * 患者ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;

        /**
         * 死者姓名（快照）
         */
        private String patientName;

        /**
         * 性别（1-男 2-女 9-未知）
         */
        private Integer gender;

        /**
         * 死亡年龄
         */
        private Integer age;

        /**
         * 死亡时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime deathTime;

        /**
         * 死亡地点（1-医院 2-来院途中 3-家中 4-民政管理机构 5-其他机构 9-未指明）
         */
        private Integer deathPlace;

        /**
         * 死亡科室ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deathDeptId;

        /**
         * 死亡科室名称（快照）
         */
        private String deathDeptName;

        /**
         * 死亡病区名称（快照）
         */
        private String deathWardName;

        /**
         * 死亡床位号（快照）
         */
        private String deathBedNo;

        /**
         * 死亡诊断
         */
        private String clinicalDiagnosis;

        /**
         * 根本死因ICD-10编码
         */
        private String underlyingIcdCode;

        /**
         * 根本死因名称（快照）
         */
        private String underlyingIcdName;

        /**
         * 是否尸检（0-否 1-是）
         */
        private Integer autopsyFlag;

        /**
         * 填表医师姓名
         */
        private String physicianName;

        /**
         * 填表时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime fillTime;

        /**
         * 审核人姓名
         */
        private String reviewerName;

        /**
         * 审核时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime reviewTime;

        /**
         * 审核意见
         */
        private String reviewOpinion;

        /**
         * 签发（出具/盖章）
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime issueTime;

        /**
         * 状态（1-草稿 2-已审核 3-已开具 4-已作废）
         */
        private Integer certStatus;

        /**
         * 死因监测上报状态（1-未上报 2-已上报 3-上报失败）
         */
        private Integer reportStatus;

        /**
         * 上报时限
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime reportDeadline;

        /**
         * 上报时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime reportTime;

        /**
         * 上报回执编号/区域死因监测编号
         */
        private String reportNo;

        /**
         * 上报失败原因
         */
        private String reportError;

        /**
         * 打印次数
         */
        private Integer printCount;

        /**
         * 最后打印时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastPrintTime;

        /**
         * 作废原因
         */
        private String voidReason;

        /**
         * 作废经办人
         */
        private String voidBy;

        /**
         * 作废时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime voidTime;

        /**
         * 重开来源证明ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long origCertId;

        /**
         * 原证编号（作废重开对照着看）
         */
        private String origCertNo;

        /**
         * 备注
         */
        private String remark;

        /**
         * 是否已逾上报时限且尚未上报成功（服务端现算）
         */
        private Boolean overdue;

        /**
         * 距上报时限剩余小时（已逾期为负；无时限为 null）
         */
        private Long remainHours;

        /**
         * 创建人
         */
        private String createBy;

        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
    }

    /**
     * 死因链行
     */
    @Data
    public static class CauseVO {
        /**
         * 主键（雪花ID）
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        private Integer part;

        private Integer seqNo;

        private String icdCode;

        private String icdName;

        private String intervalText;
    }

    /**
     * 详情 = 编辑回显：一般项目全明文（含身份证号、近亲属电话）+ 死因链 + 上报报文
     */
    @Data
    public static class Detail {
        /**
         * 主键（雪花ID）
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        /**
         * 证明编号
         */
        private String certNo;

        /**
         * 住院记录ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long admissionId;

        private String admissionNo;

        /**
         * 死亡出院记录ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long dischargeId;

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
         * 死者姓名（快照）
         */
        private String patientName;

        /**
         * 性别（1-男 2-女 9-未知）
         */
        private Integer gender;

        /**
         * 民族（快照）
         */
        private String nation;

        /**
         * 出生日期（快照）
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate birthDate;

        /**
         * 死亡年龄
         */
        private Integer age;

        /**
         * 身份证号
         */
        private String idCard;

        /**
         * 职业（快照）
         */
        private String occupation;

        /**
         * 婚姻状况（0-未婚 1-已婚 2-离异 3-丧偶）
         */
        private Integer maritalStatus;

        /**
         * 死亡时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime deathTime;

        /**
         * 死亡地点（1-医院 2-来院途中 3-家中 4-民政管理机构 5-其他机构 9-未指明）
         */
        private Integer deathPlace;

        /**
         * 死亡科室ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deathDeptId;

        /**
         * 死亡科室名称（快照）
         */
        private String deathDeptName;

        /**
         * 死亡病区名称（快照）
         */
        private String deathWardName;

        /**
         * 死亡床位号（快照）
         */
        private String deathBedNo;

        /**
         * 死亡诊断
         */
        private String clinicalDiagnosis;

        /**
         * 根本死因ICD-10编码
         */
        private String underlyingIcdCode;

        /**
         * 根本死因名称（快照）
         */
        private String underlyingIcdName;

        /**
         * 既往病史
         */
        private String pastHistory;

        /**
         * 是否尸检（0-否 1-是）
         */
        private Integer autopsyFlag;

        /**
         * 尸检结论/病理诊断
         */
        private String autopsyResult;

        /**
         * 死者近亲属姓名
         */
        private String relativeName;

        /**
         * 与死者关系
         */
        private String relativeRelation;

        /**
         * 近亲属联系电话
         */
        private String relativePhone;

        /**
         * 填表医师ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long physicianId;

        /**
         * 填表医师姓名
         */
        private String physicianName;

        /**
         * 填表时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime fillTime;

        /**
         * 审核人ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long reviewerId;

        /**
         * 审核人姓名
         */
        private String reviewerName;

        /**
         * 审核时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime reviewTime;

        /**
         * 审核意见
         */
        private String reviewOpinion;

        /**
         * 状态（1-草稿 2-已审核 3-已开具 4-已作废）
         */
        private Integer certStatus;

        /**
         * 签发（出具/盖章）
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime issueTime;

        /**
         * 打印次数
         */
        private Integer printCount;

        /**
         * 最后打印时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastPrintTime;

        /**
         * 最后打印人
         */
        private String printerName;

        /**
         * 作废原因
         */
        private String voidReason;

        /**
         * 作废经办人
         */
        private String voidBy;

        /**
         * 作废时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime voidTime;

        /**
         * 重开来源证明ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long origCertId;

        private String origCertNo;

        /**
         * 死因监测上报状态（1-未上报 2-已上报 3-上报失败）
         */
        private Integer reportStatus;

        /**
         * 上报时限
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime reportDeadline;

        /**
         * 上报时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime reportTime;

        /**
         * 上报回执编号/区域死因监测编号
         */
        private String reportNo;

        /**
         * 上报失败原因
         */
        private String reportError;

        /**
         * 上报报文（JSON 文本）；真实对接死因监测系统时的外发体
         */
        private String reportPayload;

        /**
         * 备注
         */
        private String remark;

        private List<CauseVO> causes;

        /**
         * 该次住院的重开证明（本证被作废后新开的那张），没有则 null
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long reissueCertId;

        private String reissueCertNo;

        /**
         * 关联死亡登记（一行的摘要，登记表详情走登记接口）
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long registerId;

        private String registerNo;

        private Integer registerStatus;

        /**
         * 死亡出院是否已办理（签发前置条件，前端据此提示「先去办死亡离院」）
         */
        private Boolean deathDischarged;
    }

    /**
     * 待开证榜：已办死亡离院但没有有效证明的住院（欠账榜，同传染病报卡口径）
     */
    @Data
    public static class PendingRow {
        /**
         * 住院记录ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long admissionId;

        private String admissionNo;

        /**
         * 患者ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;

        /**
         * 死者姓名（快照）
         */
        private String patientName;

        /**
         * 性别（1-男 2-女 9-未知）
         */
        private Integer gender;

        /**
         * 死亡年龄
         */
        private Integer age;

        /**
         * 科室名称
         */
        private String deptName;

        /**
         * 病区名称
         */
        private String wardName;

        /**
         * 床位号
         */
        private String bedNo;

        /**
         * 死亡出院记录ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long dischargeId;

        private String dischargeNo;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime dischargeTime;

        private String dischargeDiagnosis;

        private Integer certCount;

        /**
         * 证明编号
         */
        private String certNo;

        /**
         * 状态（1-草稿 2-已审核 3-已开具 4-已作废）
         */
        private Integer certStatus;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long certId;

        private String registerNo;

        private Integer registerStatus;

        /**
         * 出院后拖了多少天（欠账榜排序用）
         */
        private Long pendingDays;
    }

    /**
     * 住院/患者快照（写库前服务端重查，不信前端传来的死者一般项目）。
     *
     * <p>科室/病区/床位优先取<b>病案首页</b>（住院病案首页，出院后床位已释放，
     * 首页是死亡时点所在科室的留档快照），首页没有再退回入院记录现值。
     */
    @Data
    public static class PatientSnapshot {
        /**
         * 住院记录ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long admissionId;

        private String admissionNo;

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
         * 死者姓名（快照）
         */
        private String patientName;

        /**
         * 性别（1-男 2-女 9-未知）
         */
        private Integer gender;

        /**
         * 民族（快照）
         */
        private String nation;

        /**
         * 出生日期（快照）
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate birthDate;

        /**
         * 身份证号
         */
        private String idCard;

        /**
         * 职业（快照）
         */
        private String occupation;

        /**
         * 婚姻状况（0-未婚 1-已婚 2-离异 3-丧偶）
         */
        private Integer maritalStatus;

        private Integer admitStatus;

        /**
         * 入院时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime admitTime;

        /**
         * 死亡离院时间（已办「死亡」出院才有值；开证表单拿它默认填死亡时间，两处必须同一时点）
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime dischargeTime;

        /**
         * 该住院是否已有有效证明（前端据此把「开证」按钮改成「修改」）
         */
        private Boolean hasActiveCert;

        /**
         * 科室ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;

        /**
         * 科室名称
         */
        private String deptName;

        /**
         * 病区名称
         */
        private String wardName;

        /**
         * 床位号
         */
        private String bedNo;

        /**
         * 诊断
         */
        private String diagnosis;
    }

    /**
     * 死亡离院事实快照（签发前置：证明挂的这次住院必须已办「离院方式=死亡」的出院）
     */
    @Data
    public static class DischargeSnapshot {
        /**
         * 死亡出院记录ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long dischargeId;

        private String dischargeNo;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime dischargeTime;

        private Integer dischargeWay;

        private Integer deathFlag;

        private String dischargeDiagnosis;

        private String dischargeDiagnosisCode;
    }

    /**
     * 统计卡：证明四态 + 上报三态 + 登记与待开证欠账
     */
    @Data
    public static class Stats {
        /**
         * 死亡出院总数（出院记录的死亡标记=1）
         */
        private Long deathDischargeTotal;

        /**
         * 已办死亡离院但无有效证明（待开证）
         */
        private Long noCertCount;

        private Long draftCount;

        private Long auditedCount;

        private Long issuedCount;

        private Long voidCount;

        /**
         * 已开具且未上报
         */
        private Long unreportedCount;

        private Long reportedCount;

        private Long reportFailedCount;

        /**
         * 已逾上报时限且未上报成功（已开具口径）
         */
        private Long overdueCount;

        /**
         * 未登记（死亡出院但无有效登记）
         */
        private Long noRegisterCount;

        /**
         * 非疾病/死因不明且未报公安却已登记（应为 0，出现即口径破了）
         */
        private Long nonDiseaseUnpolicedCount;
    }
}

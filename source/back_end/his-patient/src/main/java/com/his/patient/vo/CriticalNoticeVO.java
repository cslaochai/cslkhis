package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 病危重通知出参。
 *
 * <p>所有 Long 主键/外键一律字符串序列化（雪花 19 位，前端 Number 会丢精度）。
 * 所有 LocalDateTime 一律空格格式（项目无全局 JSR-310 格式化配置，漏写字段就出 ISO 的 T 分隔，
 * 前端 slice(0,19) 会渲染成 '2026-09-27T19:22:48'）。
 * 身份证/电话等敏感列只在 SQL 出参层做后端脱敏（AGENTS §5），页面不渲染明文。
 */
public class CriticalNoticeVO {

    /** 输出时间统一空格分隔（与入参 @JsonFormat 同一口径，不留 T 分隔给前端二次处理） */
    private static final String TS = "yyyy-MM-dd HH:mm:ss";

    /** 台账行（不拖手写签名大字段） */
    @Data
    public static class Row {
        /** 主键（雪花ID） */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        /** 通知单号 */
        private String noticeNo;
        /** 住院记录ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long admissionId;
        /** 患者ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;
        /** 患者姓名（快照） */
        private String patientName;
        /** 住院号（快照） */
        private String admissionNo;
        /** 通知类别（1-病危 2-病重） */
        private Integer noticeType;
        /** 患者神志（1-清醒 2-嗜睡 3-意识模糊 4-昏迷 9-其他） */
        private Integer consciousnessStatus;
        /** 目前诊断 */
        private String clinicalDiagnosis;
        /** 告知时间 */
        @JsonFormat(pattern = TS)
        private LocalDateTime notifyTime;
        /** 告知医师姓名 */
        private String doctorName;
        /** 见证医师姓名（可空） */
        private String witnessDoctorName;
        /** 签收人姓名 */
        private String signerName;
        /** 签收人与患者关系 */
        private Integer signerRelation;
        /** 签收时间 */
        @JsonFormat(pattern = TS)
        private LocalDateTime acknowledgeTime;
        /** 状态（1-草稿 2-已签发 3-已签收 4-已作废） */
        private Integer noticeStatus;
        /** 电子签名状态（0-未签名 1-已签名 2-签名已作废） */
        private Integer signStatus;
        /** 签发 */
        @JsonFormat(pattern = TS)
        private LocalDateTime issueTime;
        /** 回执打印次数 */
        private Integer printCount;
        /** 最后打印时间 */
        @JsonFormat(pattern = TS)
        private LocalDateTime lastPrintTime;
        /** 开单科室名称（快照） */
        private String deptName;
        /** 病区名称（快照） */
        private String wardName;
        /** 床位号（快照） */
        private String bedNo;
        /** 作废原因 */
        private String voidReason;
        /** 备注 */
        private String remark;
        /** 创建时间 */
        @JsonFormat(pattern = TS)
        private LocalDateTime createTime;
    }

    /** 详情＝签收/打印数据源（含手写签名图与签名证据摘要；敏感列已脱敏） */
    @Data
    public static class Detail {
        /** 主键（雪花ID） */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        /** 通知单号 */
        private String noticeNo;
        /** 住院记录ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long admissionId;
        /** 患者ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;
        /** 患者姓名（快照） */
        private String patientName;
        /** 患者编号（快照） */
        private String patientNo;
        /** 性别（1-男 2-女 3-未知） */
        private Integer gender;
        /** 年龄 */
        private Integer age;
        /** 开单科室ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;
        /** 开单科室名称（快照） */
        private String deptName;
        /** 病区名称（快照） */
        private String wardName;
        /** 床位号（快照） */
        private String bedNo;
        /** 住院号（快照） */
        private String admissionNo;
        /** 通知类别（1-病危 2-病重） */
        private Integer noticeType;
        /** 患者神志（1-清醒 2-嗜睡 3-意识模糊 4-昏迷 9-其他） */
        private Integer consciousnessStatus;
        /** 目前诊断 */
        private String clinicalDiagnosis;
        /** 病情及危险因素 */
        private String conditionDesc;
        /** 可能的病情变化与预警事项 */
        private String warningMatters;
        /** 医方已采取/拟采取的诊治措施与配合要求 */
        private String doctorMeasures;
        /** 告知时间 */
        @JsonFormat(pattern = TS)
        private LocalDateTime notifyTime;
        /** 告知医师ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long doctorId;
        /** 告知医师姓名 */
        private String doctorName;
        /** 见证医师ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long witnessDoctorId;
        /** 见证医师姓名（可空） */
        private String witnessDoctorName;
        /** 签收人姓名 */
        private String signerName;
        /** 签收人与患者关系 */
        private Integer signerRelation;
        private String signerIdCardMasked;
        private String signerPhoneMasked;
        /** 签收人手写签名 */
        private String signerSignature;
        /** 签收时间 */
        @JsonFormat(pattern = TS)
        private LocalDateTime acknowledgeTime;
        /** 状态（1-草稿 2-已签发 3-已签收 4-已作废） */
        private Integer noticeStatus;
        /** 签发 */
        @JsonFormat(pattern = TS)
        private LocalDateTime issueTime;
        /** 最后打印人 */
        private String printerName;
        /** 回执打印次数 */
        private Integer printCount;
        /** 最后打印时间 */
        @JsonFormat(pattern = TS)
        private LocalDateTime lastPrintTime;
        /** 作废原因 */
        private String voidReason;
        /** 作废经办人 */
        private String voidBy;
        /** 作废时间 */
        @JsonFormat(pattern = TS)
        private LocalDateTime voidTime;
        /** 电子签名状态（0-未签名 1-已签名 2-签名已作废） */
        private Integer signStatus;
        /** 当前有效签名ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long signId;
        /** 签名时刻 */
        @JsonFormat(pattern = TS)
        private LocalDateTime signedTime;
        /** 备注 */
        private String remark;

        /** 关联签名证据（回执打印注脚：单号/摘要/证书号/验签结果） */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long signRefId;
        private String signNo;
        private String contentDigest;
        private String certNo;
        @JsonFormat(pattern = TS)
        private LocalDateTime sigSignedTime;
        private Integer verifyStatus;
    }

    /** 开单底稿：在院患者的通知对象快照（一般项目全由服务端重查，不采信前端字符串） */
    @Data
    public static class Base {
        /** 住院记录ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long admissionId;
        /** 住院号（快照） */
        private String admissionNo;
        /** 患者ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;
        /** 患者编号（快照） */
        private String patientNo;
        /** 患者姓名（快照） */
        private String patientName;
        /** 性别（1-男 2-女 3-未知） */
        private Integer gender;
        /** 年龄 */
        private Integer age;
        /** 开单科室ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;
        /** 开单科室名称（快照） */
        private String deptName;
        /** 病区名称（快照） */
        private String wardName;
        /** 床位号（快照） */
        private String bedNo;
        /** 诊断 */
        private String diagnosis;
        private Integer admitStatus;
        /** 入院时间 */
        @JsonFormat(pattern = TS)
        private LocalDateTime admitTime;
    }

    /** 在院患者候选（横幅选择器数据源） */
    @Data
    public static class Inpatient {
        /** 住院记录ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long admissionId;
        /** 住院号（快照） */
        private String admissionNo;
        /** 患者ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;
        /** 患者姓名（快照） */
        private String patientName;
        /** 开单科室名称（快照） */
        private String deptName;
        /** 病区名称（快照） */
        private String wardName;
        /** 床位号（快照） */
        private String bedNo;
        private Integer noticeCount;
    }

    /** 医师候选（告知/见证医师下拉） */
    @Data
    public static class DoctorOption {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long employeeId;
        private String empName;
        /** 开单科室名称（快照） */
        private String deptName;
    }

    /** 统计卡：四状态计数 + 待签收欠账 */
    @Data
    public static class Stats {
        private Long draftCount = 0L;
        private Long issuedCount = 0L;
        private Long ackedCount = 0L;
        private Long voidCount = 0L;
    }
}

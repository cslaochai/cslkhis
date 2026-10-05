package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 住院请假单出参（内层静态类组织同 CriticalNoticeVO）。
 *
 * <p><b>所有 Long 主键/外键一律字符串序列化</b>（雪花 19 位，前端 Number 会丢精度 —— 19 位数字
 * 超过 Number.MAX_SAFE_INTEGER，形如 2104147887607775234 会被读成 …7775200，
 * 前端拿它回查就是「请假单不存在」）。
 */
public class InpatientLeaveVO {

    /**
     * 台账行（不含手写签名大字段，敏感联系方式列表根本不选）
     */
    @Data
    public static class Row implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 主键（雪花ID）
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        /**
         * 请假单号
         */
        private String leaveNo;
        /**
         * 住院记录ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long admissionId;
        /**
         * 患者ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;
        /**
         * 患者姓名（快照）
         */
        private String patientName;
        /**
         * 住院号（快照）
         */
        private String admissionNo;
        /**
         * 科室名称（快照）
         */
        private String deptName;
        /**
         * 病区名称（快照）
         */
        private String wardName;
        /**
         * 床位号（快照）
         */
        private String bedNo;
        /**
         * 请假类别（1-临时外出当日往返 2-离院过夜 9-其他）
         */
        private Integer leaveType;
        /**
         * 请假事由（必填）
         */
        private String reason;
        /**
         * 去向
         */
        private String destination;
        /**
         * 随行/联系人姓名（必填）
         */
        private String companionName;
        /**
         * 随行人与患者关系
         */
        private Integer companionRelation;
        /**
         * 预计离院时间
         */
        private String expectedLeaveTime;
        /**
         * 预计返回时间
         */
        private String expectedReturnTime;
        /**
         * 申请时间
         */
        private String applyTime;
        /**
         * 申请人
         */
        private String applyBy;
        /**
         * 审批医师姓名
         */
        private String doctorName;
        /**
         * 状态（1-待审批 2-已批准 3-已离院 4-已返回 5-已拒绝 6-已取消）
         */
        private Integer leaveStatus;
        /**
         * 超期未归展示态（expectedReturnTime 已过且 status=3，查询时算，不落库）
         */
        private Boolean overdue;
        /**
         * 超期小时数（向下取整；未超期为 0）
         */
        private Long overdueHours;
        /**
         * 超期联系结果（1-联系上并约定返回 2-联系不上 3-家属）
         */
        private Integer overdueContactResult;
        /**
         * 上报对象（1-主管医师 2-病区护士长 3-医务科）
         */
        private Integer reportTo;
        /**
         * 承诺书打印次数
         */
        private Integer printCount;
        /**
         * 电子签名状态（0-未签名 1-已签名 2-签名已作废）
         */
        private Integer signStatus;
        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 详情 = 离院登记/打印/审批数据源：含手写签名图与关联签名证据（承诺书注脚），电话出参脱敏
     */
    @Data
    public static class Detail implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 主键（雪花ID）
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        /**
         * 请假单号
         */
        private String leaveNo;
        /**
         * 住院记录ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long admissionId;
        /**
         * 患者ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;
        /**
         * 患者姓名（快照）
         */
        private String patientName;
        /**
         * 患者编号（快照）
         */
        private String patientNo;
        /**
         * 性别（1-男 2-女 9-未知）
         */
        private Integer gender;
        /**
         * 年龄
         */
        private Integer age;
        /**
         * 申请时点所在科室ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;
        /**
         * 科室名称（快照）
         */
        private String deptName;
        /**
         * 病区名称（快照）
         */
        private String wardName;
        /**
         * 床位号（快照）
         */
        private String bedNo;
        /**
         * 住院号（快照）
         */
        private String admissionNo;
        /**
         * 请假类别（1-临时外出当日往返 2-离院过夜 9-其他）
         */
        private Integer leaveType;
        /**
         * 请假事由（必填）
         */
        private String reason;
        /**
         * 去向
         */
        private String destination;
        /**
         * 随行/联系人姓名（必填）
         */
        private String companionName;
        /**
         * 随行人与患者关系
         */
        private Integer companionRelation;
        private String companionPhoneMasked;
        /**
         * 预计离院时间
         */
        private String expectedLeaveTime;
        /**
         * 预计返回时间
         */
        private String expectedReturnTime;
        /**
         * 申请时间
         */
        private String applyTime;
        /**
         * 申请人
         */
        private String applyBy;
        /**
         * 医师意见
         */
        private String doctorAdvice;
        /**
         * 审批时间
         */
        private String approveTime;
        /**
         * 审批医师ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long doctorId;
        /**
         * 审批医师姓名
         */
        private String doctorName;
        /**
         * 拒绝理由
         */
        private String rejectReason;
        /**
         * 患方确认人姓名
         */
        private String confirmName;
        /**
         * 确认人与患者关系
         */
        private Integer confirmRelation;
        private String confirmPhoneMasked;
        /**
         * 患方手写签名
         */
        private String confirmSignature;
        /**
         * 患方签署时间
         */
        private String confirmTime;
        /**
         * 实际离院时间
         */
        private String actualLeaveTime;
        /**
         * 实际返回时间
         */
        private String actualReturnTime;
        /**
         * 返回情况备注
         */
        private String returnNote;
        /**
         * 销假经办人
         */
        private String returnBy;
        /**
         * 状态（1-待审批 2-已批准 3-已离院 4-已返回 5-已拒绝 6-已取消）
         */
        private Integer leaveStatus;
        private Boolean overdue;
        private Long overdueHours;
        /**
         * 超期联系结果（1-联系上并约定返回 2-联系不上 3-家属）
         */
        private Integer overdueContactResult;
        /**
         * 超期处置备注
         */
        private String overdueContactNote;
        /**
         * 超期处置时间
         */
        private String overdueContactTime;
        /**
         * 超期处置人
         */
        private String overdueContactBy;
        /**
         * 上报对象（1-主管医师 2-病区护士长 3-医务科）
         */
        private Integer reportTo;
        /**
         * 最后打印人
         */
        private String printerName;
        /**
         * 承诺书打印次数
         */
        private Integer printCount;
        /**
         * 最后打印时间
         */
        private String lastPrintTime;
        /**
         * 取消原因
         */
        private String cancelReason;
        /**
         * 取消经办人
         */
        private String cancelBy;
        /**
         * 取消时间
         */
        private String cancelTime;
        /**
         * 电子签名状态（0-未签名 1-已签名 2-签名已作废）
         */
        private Integer signStatus;
        /**
         * 当前有效签名ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long signId;
        /**
         * 签名时刻
         */
        private String signedTime;
        /**
         * 备注
         */
        private String remark;
        /**
         * 关联电子签名证据（签名中心可回查）
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long signRefId;
        private String signNo;
        private String contentDigest;
        private String certNo;
        private String sigSignedTime;
        private Integer verifyStatus;
        /**
         * 动作可用性（前端不自判状态，全部读后端）
         */
        private Boolean canEdit;
        private Boolean canApprove;
        private Boolean canLeave;
        private Boolean canBack;
        private Boolean canCancel;
        private Boolean canContact;
        private Boolean canPrint;
    }

    /**
     * 开单底稿：住院 + 患者一般项目（科室/病区/床位取入院现值）
     */
    @Data
    public static class Base implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 住院记录ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long admissionId;
        /**
         * 住院号（快照）
         */
        private String admissionNo;
        /**
         * 患者ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;
        /**
         * 患者编号（快照）
         */
        private String patientNo;
        /**
         * 患者姓名（快照）
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
        /**
         * 申请时点所在科室ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;
        /**
         * 科室名称（快照）
         */
        private String deptName;
        /**
         * 病区名称（快照）
         */
        private String wardName;
        /**
         * 床位号（快照）
         */
        private String bedNo;
        /**
         * 诊断
         */
        private String diagnosis;
        private Integer admitStatus;
        /**
         * 入院时间
         */
        private String admitTime;
        /**
         * 该住院当前在途请假单张数（status 1/2/3；>0 时不可再申请）
         */
        private Integer activeLeaveCount;
    }

    /**
     * 在院患者候选（护士站/医生站横幅数据源，含在途请假单状态）
     */
    @Data
    public static class Inpatient implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 住院记录ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long admissionId;
        /**
         * 住院号（快照）
         */
        private String admissionNo;
        /**
         * 患者ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;
        /**
         * 患者姓名（快照）
         */
        private String patientName;
        /**
         * 科室名称（快照）
         */
        private String deptName;
        /**
         * 病区名称（快照）
         */
        private String wardName;
        /**
         * 床位号（快照）
         */
        private String bedNo;
        /**
         * 在途请假单张数（status 1/2/3）
         */
        private Integer activeLeaveCount;
        /**
         * 当前是否已离院在途（status=3）
         */
        private Integer leftStatus;
    }

    /**
     * 统计卡：待审批 / 已批准待离院 / 在院外 / 超期未归 / 今日应返回
     */
    @Data
    public static class Stats implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long pendingCount;
        private Long approvedCount;
        private Long leftCount;
        private Long overdueCount;
        private Long returnedTodayCount;
    }
}

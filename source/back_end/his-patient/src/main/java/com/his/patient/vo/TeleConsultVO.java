package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 远程会诊 VO。
 *
 * <p>按钮可用性（canEdit/canArrange/canComplete/canCancel/canDelete）一律服务端派生，
 * 前端不按 status 码值 switch。
 */
@Data
public class TeleConsultVO implements Serializable {

    /** 主键ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 会诊单号 */
    private String consultNo;

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 患者编号（快照） */
    private String patientNo;

    /** 患者姓名（快照） */
    private String patientName;

    /** 关联住院ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /** 申请科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyDeptId;

    /** 申请科室名称（快照） */
    private String applyDeptName;

    /** 申请医生ID（员工ID） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyDoctorId;

    /** 申请医生姓名 */
    private String applyDoctor;

    /** 会诊类型（1-临床会诊 2-远程影像 3-远程心电 4-远程病理 5-其他） */
    private Integer consultType;

    /** 是否急会诊（0-否 1-是） */
    private Integer isUrgent;

    /** 受邀专家所在医院 */
    private String expertHospital;

    /** 受邀专家科室 */
    private String expertDept;

    /** 受邀专家姓名 */
    private String expertName;

    /** 受邀专家职称 */
    private String expertTitle;

    /** 会诊目的 */
    private String purpose;

    /** 申请方诊断/病情摘要 */
    private String diagnosis;

    /** 计划会诊时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planTime;

    /** 计划时长（分钟） */
    private Integer durationMin;

    /** 对接平台 */
    private String platform;

    /** 接入号/会议室号（预留） */
    private String meetNo;

    /** 会诊意见 */
    private String opinion;

    /** 状态（1-待安排 2-已安排 3-已完成 4-已取消） */
    private Integer status;

    /** 会诊费用 */
    private BigDecimal fee;

    /** 安排人 */
    private String arrangeBy;

    /** 安排时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime arrangeTime;

    /** 完成人 */
    private String completeBy;

    /** 完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completeTime;

    /** 取消原因 */
    private String cancelReason;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 备注 */
    private String remark;

    /** 仅待安排可改 */
    private Boolean canEdit;

    /** 仅待安排可安排 */
    private Boolean canArrange;

    /** 仅已安排可出意见完成 */
    private Boolean canComplete;

    /** 非终态可取消 */
    private Boolean canCancel;

    /** 仅待安排可删 */
    private Boolean canDelete;
}

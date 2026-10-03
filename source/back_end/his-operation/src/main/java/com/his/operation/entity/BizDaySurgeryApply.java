package com.his.operation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 日间手术登记单。
 *
 * <p>状态机单向：1待评估 → 2评估通过 → 3已安排 → 4术后观察 → 5已出院（终态）；
 * 未终态 → 6已取消（原因必填，终态）；术后观察 → 7已转住院（住院单必填，终态）。
 * <b>评估未通过不得安排、未安排不得登记完成</b> —— 评审必查的硬闸门。
 */
@Data
@TableName("biz_day_surgery_apply")
public class BizDaySurgeryApply {

    public static final int STATUS_WAIT_EVAL = 1;
    public static final int STATUS_EVAL_PASSED = 2;
    public static final int STATUS_ARRANGED = 3;
    public static final int STATUS_OBSERVING = 4;
    public static final int STATUS_DISCHARGED = 5;
    public static final int STATUS_CANCELED = 6;
    public static final int STATUS_TRANSFERRED = 7;

    /** 术前评估结论：通过 */
    public static final int EVAL_PASS = 1;
    /** 术前评估结论：不通过 */
    public static final int EVAL_FAIL = 2;

    /** 离院方式：按时离院 */
    public static final int LEAVE_NORMAL = 1;
    /** 离院方式：转普通住院 */
    public static final int LEAVE_TRANSFER = 2;
    /** 离院方式：非计划再入院 */
    public static final int LEAVE_READMIT = 3;

    /** 随访时限（小时）：出院/转住院后 24 小时内必访 */
    public static final int FOLLOW_DUE_HOURS = 24;

    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 登记单号 */
    private String applyNo;

    /** 准入术式ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long itemId;

    /** 术式编码（快照） */
    private String itemCode;

    /** 术式名称（快照） */
    private String itemName;

    /** 最长滞留小时数 */
    private Integer maxStayHours;

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 患者编号（快照） */
    private String patientNo;

    /** 患者姓名（快照） */
    private String patientName;

    /** 手术科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 手术科室名称（快照） */
    private String deptName;

    /** 手术医生ID（员工ID） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /** 手术医生姓名 */
    private String doctorName;

    /** 计划手术日期 */
    private LocalDate planSurgeryDate;

    /** 状态（1-待评估 2-评估通过 3-已安排 4-术后观察 5-已出院 6-已取消 7-已转住院） */
    private Integer status;

    /** 术前评估结论（1-通过 2-不通过） */
    private Integer evalResult;

    /** 评估人 */
    private String evalBy;

    /** 评估时间 */
    private LocalDateTime evalTime;

    /** 评估意见/禁忌筛查结果 */
    private String evalRemark;

    /** 手术开始时间 */
    private LocalDateTime surgeryTime;

    /** 手术间 */
    private String operatingRoom;

    /** 台次 */
    private Integer seqNo;

    /** 实际麻醉方式 */
    private Integer anesthesiaType;

    /** 主刀医生姓名 */
    private String surgeon;

    /** 安排人 */
    private String arrangeBy;

    /** 安排时间 */
    private LocalDateTime arrangeTime;

    /** 手术结束时间 */
    private LocalDateTime surgeryEndTime;

    /** 离院方式（1-按时离院 2-转普通住院 3-非计划再入院） */
    private Integer leaveType;

    /** 离院时间 */
    private LocalDateTime dischargeTime;

    /** 离院登记人 */
    private String dischargeBy;

    /** 出院评估结论/医嘱交代 */
    private String dischargeRemark;

    /** 转住院的住院ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long transferAdmissionId;

    /** 转住院原因 */
    private String transferRemark;

    /** 随访次数 */
    private Integer followCount;

    /** 取消原因 */
    private String cancelReason;

    /** 创建人 */
    private String createBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新人 */
    private String updateBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 删除标志（0-正常 1-删除） */
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    /** 备注 */
    private String remark;
}

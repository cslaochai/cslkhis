package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 远程会诊（院际 / 跨院专家会诊）。
 */
@Data
@TableName("biz_tele_consult")
public class BizTeleConsult {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 会诊单号
     */
    private String consultNo;

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
     * 关联住院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 申请科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyDeptId;

    /**
     * 申请科室名称
     */
    private String applyDeptName;

    /**
     * 申请医生ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyDoctorId;

    /**
     * 申请医生姓名
     */
    private String applyDoctor;

    /**
     * 会诊类型（1-临床会诊 2-远程影像 3-远程心电 4-远程病理 5-其他）
     */
    private Integer consultType;

    /**
     * 是否急会诊（0-否 1-是）
     */
    private Integer isUrgent;

    /**
     * 受邀专家所在医院
     */
    private String expertHospital;

    /**
     * 受邀专家科室
     */
    private String expertDept;

    /**
     * 受邀专家姓名
     */
    private String expertName;

    /**
     * 受邀专家职称
     */
    private String expertTitle;

    /**
     * 会诊目的
     */
    private String purpose;

    /**
     * 申请方诊断/病情摘要
     */
    private String diagnosis;

    /**
     * 计划会诊时间
     */
    private LocalDateTime planTime;

    /**
     * 计划时长（分钟）
     */
    private Integer durationMin;

    /**
     * 对接平台
     */
    private String platform;

    /**
     * 接入号/会议室号（预留）
     */
    private String meetNo;

    /**
     * 会诊意见
     */
    private String opinion;

    /**
     * 状态（1-待安排 2-已安排 3-已完成 4-已取消）
     */
    private Integer status;

    /**
     * 会诊费用
     */
    private BigDecimal fee;

    /**
     * 安排人
     */
    private String arrangeBy;

    /**
     * 安排时间
     */
    private LocalDateTime arrangeTime;

    /**
     * 完成人
     */
    private String completeBy;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标志（0-正常 1-删除）
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}

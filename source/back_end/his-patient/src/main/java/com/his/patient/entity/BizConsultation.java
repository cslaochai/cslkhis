package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 住院会诊（P4.1：申请 → 应答 → 会诊记录 → 完成 → 回写病历）。
 */
@Data
@TableName("biz_consultation")
public class BizConsultation implements Serializable {

    /**
     * 会诊ID
     */
    @TableId(value = "consultation_id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long consultationId;

    /**
     * 会诊编号（会诊号：HZ + yyyyMMdd + 4位序号）
     */
    private String consultationNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 就诊次ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long visitId;

    /**
     * 入院ID（住院会诊必填；门诊会诊为空。本期只做住院侧闭环）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 申请科室ID（= 申请时会诊发起方所在科室，入院科室的快照语义）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromDeptId;

    /**
     * 申请医生ID（员工ID，不是用户的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyDoctorId;

    /**
     * 申请医生姓名
     */
    private String applyDoctorName;

    /**
     * 会诊科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long toDeptId;

    /**
     * 会诊范围：1-科内 2-科间 3-全院
     */
    private Integer consultType;

    /**
     * 会诊类别：1-普通科间 2-营养会诊 3-药学会诊 4-其他专科（sql/168 §5）
     *
     * <p>闭环（申请→应答→完成/取消）与 {@code consult_type} 完全共用，
     * 类别只决定"这单归谁处理、在哪个工作台出现"，不另起一套状态机。
     */
    private Integer consultCategory;

    /**
     * 是否急会诊：0-普通 1-急会诊（10 分钟内响应、队列置顶）
     */
    private Integer isUrgent;

    /**
     * 会诊理由（申请时的目的与要解决的问题，不允许空）
     */
    private String reason;

    /**
     * 会诊医生ID：0 = 申请时未指定（等会诊科室认领）；接诊后写实际接诊医生
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;

    /**
     * 会诊时间（既有列：会诊实际发生/出结论的时刻）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime consultTime;

    /**
     * 会诊状态：0-待应答 1-已完成 2-已取消 3-已应答（会诊中）
     */
    private Integer consultStatus;

    /**
     * 会诊方接诊时间（应答留痕）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acceptTime;

    /**
     * 接诊医生ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long acceptDoctorId;

    /**
     * 接诊医生姓名
     */
    private String acceptDoctorName;

    /**
     * 会诊完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;

    /**
     * 回写的住院病历ID（住院病历文书的ID，四核对"病历"一侧的锚点）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 会诊结论
     */
    private String conclusion;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人
     */
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标志（0-正常 1-删除；会诊留痕不提供物理删除）
     */
    @TableLogic
    private Integer delFlag;
}

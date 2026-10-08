package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 互联网线上问诊（患者发起 → 医生接诊 → 回复 → 完成 / 退诊）。
 */
@Data
@TableName("biz_online_consult")
public class BizOnlineConsult {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 问诊单号
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
     * 接诊科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 接诊科室名称
     */
    private String deptName;

    /**
     * 接诊医生ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 接诊医生姓名
     */
    private String doctorName;

    /**
     * 问诊方式（1-图文问诊 2-电话问诊 3-视频问诊）
     */
    private Integer consultType;

    /**
     * 主诉/问题描述
     */
    private String chiefComplaint;

    /**
     * 医生回复
     */
    private String reply;

    /**
     * 处置建议
     */
    private String advice;

    /**
     * 是否建议线下就诊（0-否 1-是）
     */
    private Integer needVisit;

    /**
     * 状态（1-待接诊 2-接诊中 3-已完成 4-已退诊）
     */
    private Integer status;

    /**
     * 问诊费用
     */
    private BigDecimal fee;

    /**
     * 发起时间
     */
    private LocalDateTime applyTime;

    /**
     * 接诊人
     */
    private String acceptBy;

    /**
     * 接诊时间
     */
    private LocalDateTime acceptTime;

    /**
     * 完成人
     */
    private String finishBy;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 退诊原因
     */
    private String rejectReason;

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

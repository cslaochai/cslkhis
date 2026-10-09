package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 字段级修改日志（字段级修改日志，建表见 sql/159）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_field_change_log")
public class SysFieldChangeLog extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 对象类型：PATIENT-患者 USER-系统用户 EMPLOYEE-员工 MEDICAL_RECORD-病历 INPATIENT_RECORD-住院文书
     */
    private String bizType;

    /**
     * 对象ID（字符串存，各对象主键类型不一）
     */
    private String bizId;

    /**
     * 对象编号快照（患者号/工号/病历号）
     */
    private String bizNo;

    /**
     * 对象名称快照（患者姓名/用户名）
     */
    private String bizName;

    /**
     * 字段英文名
     */
    private String fieldName;

    /**
     * 字段中文名（审计员看的是这个）
     */
    private String fieldLabel;

    /**
     * 变更前值（直接标识符已打码）
     */
    private String oldValue;

    /**
     * 变更后值（直接标识符已打码）
     */
    private String newValue;

    /**
     * 变更类型：INSERT-建档 UPDATE-修改 ACTION-操作留痕
     */
    private String changeType;

    /**
     * 批次号：同一次保存的多个字段共用一个
     */
    private String batchNo;

    /**
     * 操作人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 操作人科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 操作人科室名称
     */
    private String deptName;

    /**
     * 变更时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime changeTime;
}

package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 出院办理（出院记录）
 * <p>既有表，主键出院ID，不继承 BaseEntity。
 * {@code discharge_way}（离院方式）是病案首页必填项，也是 DRG 分组与再入院判定的输入。
 */
@Data
@TableName("biz_discharge")
public class BizDischarge implements Serializable {

    /** 出院ID */
    @TableId(value = "discharge_id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dischargeId;

    /** 出院记录号（DIS + yyyyMMdd + 3位序号） */
    private String dischargeNo;

    /** 入院ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 出院时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dischargeTime;

    /** 出院医生ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dischargeDoctorId;

    /** 出院诊断（文本，兼容既有数据） */
    private String dischargeDiagnosis;

    /** 出院诊断ICD编码 */
    private String dischargeDiagnosisCode;

    /**
     * 离院方式：1-医嘱离院 2-医嘱转院 3-医嘱转社区 4-非医嘱离院 5-死亡 9-其他
     * <p>病案首页必填；死亡时必须为 5。
     */
    private Integer dischargeWay;

    /** 死亡标志（0-否 1-是） */
    private Integer deathFlag;

    /** 出院小结 / 出院带药医嘱 */
    private String dischargeSummary;

    /** 状态：1-正常 2-转科 3-自动出院（既有字段，兼容保留） */
    private Integer dischargeStatus;

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新人 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 删除标志（0-正常 1-删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;
}

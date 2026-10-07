package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 入院登记（入院记录）
 */
@Data
@TableName("biz_admission")
public class BizAdmission implements Serializable {

    /**
     * 入院ID
     */
    @TableId(value = "admission_id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 入院记录号（ADM + yyyyMMdd + 3位序号）
     */
    private String admissionNo;

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
     * 来源挂号ID（门诊转住院时写入；既有数据为 NULL，不猜它当初从哪来）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 来源挂号号（快照）
     */
    private String registNo;

    /**
     * 来源住院证ID（入院通知单的ID）——收治闭环的回指
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionOrderId;

    /**
     * 入院科室ID（**入院时写死，转科不改** —— 病案首页"入院科别"的唯一来源）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admitDeptId;

    /**
     * 当前科室ID（随转科更新；首次转科后与 admitDeptId 不再相等）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /**
     * 床位ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bedId;

    /**
     * 入院医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admitDoctorId;

    /**
     * 入院时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime admitTime;

    /**
     * 入院途径：1-门诊 2-急诊 3-转院 4-其他（病案首页字段；既有数据为 NULL，读取侧不得默认成门诊）
     */
    private Integer admitWay;

    /**
     * 入院诊断（文本，兼容既有数据）
     */
    private String diagnosis;

    /**
     * 入院诊断ICD编码
     */
    private String admitDiagnosisCode;

    /**
     * 入院诊断名称
     */
    private String admitDiagnosisName;

    /**
     * 出院时间（出院时写入，冗余便于列表查询）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dischargeTime;

    /**
     * 状态（0-已出院 1-在院）
     */
    private Integer admitStatus;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标志（0-正常 1-删除）
     */
    @TableLogic
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}

package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 住院责任护士关系（与主管医生管床关系并列的护理线归属）。
 *
 * <p>主管医生走 {@code biz_attending_relation}，责任护士单独建表：医生关系里硬编码校验
 * 「必须是医生」，且 relation_type 表达的是医生团队分工（主管/主诊组长/协作），塞护士会破坏语义。
 * 责任护士一次住院只有一名主责，转交时旧记录置失效留痕、不删行。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_charge_nurse_relation")
public class BizChargeNurseRelation extends BaseEntity {

    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;

    /** 住院登记ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 患者姓名（快照） */
    private String patientName;

    /** 责任护士ID（sys_employee.id，岗位类别=护理） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long nurseId;

    /** 责任护士姓名（快照） */
    private String nurseName;

    /** 科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 科室名称（快照） */
    private String deptName;

    /** 病区ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /** 床位ID（快照，便于按床反查责任护士） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bedId;

    /**
     * 关系类型（1-责任护士，预留协助护士扩展）
     */
    private Integer relationType;

    /** 状态（1-有效 0-已结束） */
    private Integer status;

    /** 生效时间（一般＝入院时间或交接时间） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveTime;

    /** 失效时间（出院或转交时填） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;
}

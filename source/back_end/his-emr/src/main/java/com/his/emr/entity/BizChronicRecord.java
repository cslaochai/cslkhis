package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 慢病建档/认定实体（M1）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_chronic_record")
public class BizChronicRecord extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 档案编号
     */
    private String recordNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 慢病编码（ICD-10）
     */
    private String diseaseCode;

    /**
     * 慢病名称
     */
    private String diseaseName;

    /**
     * 认定医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 认定医生姓名
     */
    private String doctorName;

    /**
     * 认定科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 认定科室名称
     */
    private String deptName;

    /**
     * 认定状态（0-待认定 1-已认定 2-已取消）
     */
    private Integer confirmStatus;

    /**
     * 认定时间
     */
    private LocalDateTime confirmTime;
}

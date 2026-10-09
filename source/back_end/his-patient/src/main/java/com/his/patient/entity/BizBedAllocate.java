package com.his.patient.entity;

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
 * 床位调配台账
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_bed_allocate")
public class BizBedAllocate extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 调配单号（TP + yyyyMMdd + 3位序号）
     */
    private String allocateNo;

    /**
     * 床位ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bedId;

    /**
     * 床位号
     */
    private String bedNo;

    /**
     * 病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /**
     * 病区名称
     */
    private String wardName;

    /**
     * 床位归属科室ID（资产口径）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ownDeptId;

    /**
     * 床位归属科室名称
     */
    private String ownDeptName;

    /**
     * 实际使用科室ID（跨科调配时 ≠ ownDeptId）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long useDeptId;

    /**
     * 实际使用科室名称
     */
    private String useDeptName;

    /**
     * 来源等床记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long waitId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 调配类型（1-本科室预留 2-跨科调配 3-急诊占床）
     */
    private Integer allocType;

    /**
     * 状态（1-已预留 2-已转入院 3-已释放 4-已作废）
     */
    private Integer allocStatus;

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
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operateTime;

    /**
     * 释放时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime releaseTime;

    /**
     * 释放/作废原因
     */
    private String releaseReason;

    /**
     * 转入院后的入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;
}

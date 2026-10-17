package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 临床路径入径记录（临床路径入径记录）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_pathway_enroll")
public class BizPathwayEnroll extends BaseEntity implements Serializable {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 入径单号（LP + yyyyMMdd + 4位流水号）
     */
    private String enrollNo;

    /**
     * 模板ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pathwayId;

    /**
     * 路径编码
     */
    private String pathwayCode;

    /**
     * 路径名称
     */
    private String pathwayName;

    /**
     * 版本号
     */
    private String version;

    /**
     * 路径总日数
     */
    private Integer totalDays;

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

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
     * 入院科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 入院科室名称
     */
    private String deptName;

    /**
     * 入院诊断（快照，入径依据）
     */
    private String diagnosis;

    /**
     * 入径日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate enrollDate;

    /**
     * 入径操作人
     */
    private String enrollBy;

    /**
     * 入径时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime enrollTime;

    /**
     * 状态（1-在径 2-已完成 3-已退径）
     */
    private Integer status;

    /**
     * 完成/退径日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate finishDate;

    /**
     * 完成/退径操作人
     */
    private String finishBy;

    /**
     * 退径原因（status=3 必填）
     */
    private String abortReason;

    /**
     * 变异次数（登记变异时回算的冗余派生值）
     */
    private Integer varianceCount;
}

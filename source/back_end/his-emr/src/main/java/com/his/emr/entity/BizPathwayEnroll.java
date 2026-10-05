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

/**
 * 临床路径入径记录（临床路径入径记录）。
 *
 * <p>一次住院同时只允许一条在径（服务层校验）。患者/科室/模板信息全部快照，
 * 模板停用或改版本不影响存量入径推进。路径日 currentDay 为派生值不落库。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_pathway_enroll")
public class BizPathwayEnroll extends BaseEntity implements Serializable {

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
     * 路径编码（快照）
     */
    private String pathwayCode;

    /**
     * 路径名称（快照）
     */
    private String pathwayName;

    /**
     * 版本号（快照）
     */
    private String version;

    /**
     * 路径总日数（快照）
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
     * 患者编号（快照）
     */
    private String patientNo;

    /**
     * 患者姓名（快照）
     */
    private String patientName;

    /**
     * 入院科室ID（快照）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 入院科室名称（快照）
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

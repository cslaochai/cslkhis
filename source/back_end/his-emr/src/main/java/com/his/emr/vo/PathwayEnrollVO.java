package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 临床路径入径记录 VO（快照 + 派生路径日；详情回填模板步骤与变异台账）。
 */
@Data
public class PathwayEnrollVO implements Serializable {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 入径单号
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
     * 入院诊断
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
     * 退径原因
     */
    private String abortReason;

    /**
     * 变异次数
     */
    private Integer varianceCount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 当前路径日（派生：min(total_days, 今天 - 入径日期 + 1)；终态停在终止日）
     */
    private Integer currentDay;

    /**
     * 模板步骤（仅详情回填）
     */
    private List<PathwayStepVO> steps;

    /**
     * 变异台账（仅详情回填）
     */
    private List<PathwayVarianceVO> variances;
}

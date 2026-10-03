package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 临床路径模板 VO（模板 + 步骤；步骤仅详情回填）。
 */
@Data
public class PathwayVO implements Serializable {

    /** 主键ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 路径编码 */
    private String pathwayCode;

    /** 路径名称 */
    private String pathwayName;

    /** 适用科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 适用科室名称（快照） */
    private String deptName;

    /** 适用病种/诊断 */
    private String diagnosis;

    /** 版本号 */
    private String version;

    /** 路径总日数 */
    private Integer totalDays;

    /** 状态（1-草稿 2-使用中 3-已停用） */
    private Integer status;

    /** 发布人 */
    private String publishBy;

    /** 发布时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 备注 */
    private String remark;

    private List<PathwayStepVO> steps;
}

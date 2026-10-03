package com.his.emr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 入径台账分页查询入参（enrollListPage 为 POST）。
 */
@Data
public class EnrollQueryPageDTO implements Serializable {

    /** 模板ID */
    private Long pathwayId;

    /** 入院科室ID（快照） */
    private Long deptId;

    /** 状态:1-在径 2-已完成 3-已退径 */
    private Integer status;

    /** 入径日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate enrollDate;

    /** 患者姓名（快照） */
    private String patientName;

    /** 页码 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;
}

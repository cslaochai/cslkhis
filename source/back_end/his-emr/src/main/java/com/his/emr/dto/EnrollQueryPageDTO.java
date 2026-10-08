package com.his.emr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 入径台账分页查询入参（enrollListPage 为 POST）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EnrollQueryPageDTO extends PageParam implements Serializable {

    /**
     * 模板ID
     */
    private Long pathwayId;

    /**
     * 入院科室ID
     */
    private Long deptId;

    /**
     * 状态:1-在径 2-已完成 3-已退径
     */
    private Integer status;

    /**
     * 入径日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate enrollDate;

    /**
     * 患者姓名
     */
    private String patientName;
}
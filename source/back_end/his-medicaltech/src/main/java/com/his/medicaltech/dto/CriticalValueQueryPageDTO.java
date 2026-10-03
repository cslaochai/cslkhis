package com.his.medicaltech.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 危急值分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CriticalValueQueryPageDTO extends PageParam {

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 闭环状态（1-待接收 2-已接收 3-已处置 4-已作废）
     */
    private Integer status;

    /**
     * 危急值类型（1-偏低 2-偏高）
     */
    private Integer criticalType;

    /**
     * 关键词：危急值号 / 患者姓名 / 检验项目
     */
    private String keyword;

    /**
     * 报告开始日期（yyyy-MM-dd）
     */
    private String startDate;

    /**
     * 报告结束日期（yyyy-MM-dd）
     */
    private String endDate;

    /**
     * 只看超时未处置
     */
    private Boolean overdueOnly;
}

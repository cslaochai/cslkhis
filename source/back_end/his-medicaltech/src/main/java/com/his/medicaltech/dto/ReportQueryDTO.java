package com.his.medicaltech.dto;

import lombok.Data;

/**
 * 报表统计查询入参
 */
@Data
public class ReportQueryDTO {

    /**
     * 开始日期，格式：yyyy-MM-dd
     */
    private String startDate;

    /**
     * 结束日期，格式：yyyy-MM-dd
     */
    private String endDate;
}

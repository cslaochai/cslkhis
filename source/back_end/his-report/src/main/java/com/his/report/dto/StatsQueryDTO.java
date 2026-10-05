package com.his.report.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 报表统计区间查询 DTO（yyyy-MM-dd，两侧可空由服务层兜底默认区间）
 */
@Data
public class StatsQueryDTO {

    /**
     * 开始日期
     */
    @Pattern(regexp = "^$|^\\d{4}-\\d{2}-\\d{2}$", message = "开始日期格式应为 yyyy-MM-dd")
    private String startDate;

    /**
     * 结束日期
     */
    @Pattern(regexp = "^$|^\\d{4}-\\d{2}-\\d{2}$", message = "结束日期格式应为 yyyy-MM-dd")
    private String endDate;
}

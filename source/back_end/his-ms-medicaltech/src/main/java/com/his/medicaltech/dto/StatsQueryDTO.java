package com.his.medicaltech.dto;

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

    /**
     * 科室筛选（可空，空 = 全院口径）。仅对挂科室列的门诊/住院/费用/处方主表生效；
     * 账单、支付流水、发药记录、床位快照不挂科室，维持全院口径。
     */
    private Long deptId;
}

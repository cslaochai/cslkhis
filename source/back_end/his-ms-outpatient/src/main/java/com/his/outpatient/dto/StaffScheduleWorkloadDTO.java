package com.his.outpatient.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 员工排班月度工时统计入参（按排班单元 × 岗位类别汇总，含整段未排班的人）。
 */
@Data
public class StaffScheduleWorkloadDTO {

    /**
     * 统计月份（yyyy-MM）
     */
    @NotBlank(message = "统计月份不能为空")
    private String month;

    /**
     * 排班单元类型（1-科室 2-病区 3-全院），空=全部单元
     */
    private Integer orgType;

    /**
     * 排班单元ID，空=该类型全部单元
     */
    private Long orgId;

    /**
     * 岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他），空=全部岗位
     */
    private Integer staffType;
}

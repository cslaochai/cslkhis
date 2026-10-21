package com.his.outpatient.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 全院总值班月度工时统计入参（按人汇总白班/夜班次数与总时长）。
 */
@Data
public class DutyWorkloadDTO {

    /**
     * 统计月份（yyyy-MM）
     */
    @NotBlank(message = "统计月份不能为空")
    private String month;

    /**
     * 值班点位ID，空=全部点位
     */
    private Long postId;
}

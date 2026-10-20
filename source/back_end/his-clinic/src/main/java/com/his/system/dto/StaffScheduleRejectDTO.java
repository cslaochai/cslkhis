package com.his.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 全院岗位排班「驳回」入参：把待审核的排班退回草稿，并附带驳回原因。
 */
@Data
public class StaffScheduleRejectDTO {

    /**
     * 排班行ID
     */
    @NotNull(message = "排班行ID不能为空")
    private Long id;

    /**
     * 驳回原因（留痕展示，便于提交人知道为什么被打回）
     */
    private String reason;
}

package com.his.emr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 随访电话外呼登记入参
 */
@Data
public class FollowupCallRegisterDTO {

    /**
     * 随访任务ID
     */
    @NotNull(message = "随访任务ID不能为空")
    private Long id;
}

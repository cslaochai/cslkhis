package com.his.appoint.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 停诊批量退号入参
 */
@Data
public class ScheduleBatchCancelDTO {

    @NotEmpty(message = "挂号记录不能为空")
    private List<Long> registIds;

    /**
     * 原因
     */
    private String reason;
}

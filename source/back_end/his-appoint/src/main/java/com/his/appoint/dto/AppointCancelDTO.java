package com.his.appoint.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 退号入参
 */
@Data
public class AppointCancelDTO {
    /**
     * 挂号记录ID
     */
    @NotNull(message = "挂号记录ID不能为空")
    private Long registId;
    /**
     * 退号原因
     */
    private String reason;
}

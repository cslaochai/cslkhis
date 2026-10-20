package com.his.spi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 按挂号记录签到入参
 */
@Data
public class AppointCheckInUpdateDTO {

    /**
     * 挂号记录ID
     */
    @NotNull(message = "挂号id不能为空")
    private Long registId;
}

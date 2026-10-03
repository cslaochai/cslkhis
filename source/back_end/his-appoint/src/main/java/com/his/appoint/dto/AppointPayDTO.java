package com.his.appoint.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 确认支付入参
 */
@Data
public class AppointPayDTO {
    /**
     * 队列ID
     */
    @NotNull(message = "队列id不能为空！")
    private Long id;
    /**
     * 挂号记录ID
     */
    @NotNull(message = "挂号id不能为空！")
    private Long registId;
    /**
     * 支付方式
     */
    @NotNull(message = "支付方式不能为空！")
    private Integer paymentMethod;
}

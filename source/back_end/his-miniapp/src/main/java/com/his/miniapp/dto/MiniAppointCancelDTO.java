package com.his.miniapp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 患者端退号入参（本地契约：只带挂号记录ID与退号原因，取消动作走 AppointFacadeService#cancelRegist）。
 */
@Data
public class MiniAppointCancelDTO {

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

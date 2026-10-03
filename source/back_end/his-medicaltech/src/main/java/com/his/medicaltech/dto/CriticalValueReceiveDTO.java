package com.his.medicaltech.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 危急值接收入参
 */
@Data
public class CriticalValueReceiveDTO {

    /**
     * 危急值ID
     */
    @NotNull(message = "危急值ID不能为空")
    private Long criticalValueId;

    /**
     * 接收人；为空时取当前登录账号
     */
    private String receiveBy;
}

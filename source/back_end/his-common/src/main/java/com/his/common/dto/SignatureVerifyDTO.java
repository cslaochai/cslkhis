package com.his.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 验签入参（按签名ID）。
 */
@Data
public class SignatureVerifyDTO {

    /** 当前有效签名ID */
    @NotNull(message = "签名ID不能为空")
    private Long signId;
}

package com.his.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 作废签名入参。作废**必须写理由** —— 一条没有理由的作废，
 */
@Data
public class SignatureInvalidateDTO {

    /**
     * 当前有效签名ID
     */
    @NotNull(message = "签名ID不能为空")
    private Long signId;

    /**
     * 作废原因（必填）
     */
    private String reason;
}

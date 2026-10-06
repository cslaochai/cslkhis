package com.his.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * UDI 扫码解析入参
 */
@Data
public class UdiScanDTO {
    /**
     * UDI 原文（扫码枪整串）
     */
    @NotBlank(message = "UDI码不能为空，请扫码或粘贴完整码串")
    private String udiCode;
}

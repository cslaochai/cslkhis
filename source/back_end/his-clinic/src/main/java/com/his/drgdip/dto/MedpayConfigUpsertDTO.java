package com.his.drgdip.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 医保付费主参数保存 DTO（POST /report/dip/config）。
 */
@Data
public class MedpayConfigUpsertDTO implements Serializable {

    /**
     * 住院结算主付费方式：DRG / DIP（medpay.mode）
     */
    @NotBlank(message = "主付费方式不能为空")
    private String mode;

    /**
     * DIP 病种库默认版本（dip.default_version，如 2.0 / 3.0）
     */
    @NotBlank(message = "DIP 病种库版本不能为空")
    private String dipVersion;

    /**
     * DIP 点值（元/分值，dip.point_value）；为空表示未配置，结算金额不计算
     */
    private String pointValue;
}

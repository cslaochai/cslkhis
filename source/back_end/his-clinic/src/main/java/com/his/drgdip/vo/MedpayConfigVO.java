package com.his.drgdip.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 医保付费主参数 VO（GET /report/dip/config 响应）。
 * 主付费方式 + DIP 病种库版本 + DIP 点值，落库于 sys_config（medpay.mode / dip.default_version / dip.point_value）。
 */
@Data
public class MedpayConfigVO implements Serializable {

    /**
     * 住院结算主付费方式：DRG / DIP（medpay.mode，默认 DRG）
     */
    private String mode;

    /**
     * DIP 病种库默认版本（dip.default_version，如 2.0 / 3.0）
     */
    private String dipVersion;

    /**
     * DIP 点值（元/分值，dip.point_value）；未配置为 null，结算金额不计算
     */
    private String pointValue;
}

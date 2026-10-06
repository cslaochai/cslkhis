package com.his.charge.dto;

import lombok.Data;

/**
 * 收费结算试算入参
 */
@Data
public class SettlementPreviewDTO {

    /**
     * 收费单ID
     */
    private Long chargeId;

    /**
     * 结算方式：1-自费 2-医保（为空时按患者参保信息自动判定）
     */
    private Integer settlementMode;

    /**
     * 医保类型：城镇职工医保 / 城乡居民医保 / 公费医疗（结算方式为医保时生效）
     */
    private String insuranceType;
}

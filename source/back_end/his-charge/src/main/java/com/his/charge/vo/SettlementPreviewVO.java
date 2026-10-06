package com.his.charge.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 收费结算试算出参（含医保计算与支付二维码）
 */
@Data
public class SettlementPreviewVO {

    /**
     * 收费单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long chargeId;

    /**
     * 收费单号
     */
    private String chargeNo;

    /**
     * 患者号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 应收总额，单位：元
     */
    private BigDecimal totalAmount;

    /**
     * 结算方式：1-自费 2-医保
     */
    private Integer settlementMode;

    /**
     * 结算方式名称：自费 / 医保
     */
    private String settlementModeName;

    /**
     * 医保类型：城镇职工医保 / 城乡居民医保 / 公费医疗
     */
    private String insuranceType;

    /**
     * 是否医保结算
     */
    private Boolean insurancePatient;

    /**
     * 统筹报销比例（%）
     */
    private BigDecimal coverageRatio;

    /**
     * 乙类自付比例（%）
     */
    private BigDecimal selfPayRatio;

    /**
     * 医保统筹支付金额，单位：元
     */
    private BigDecimal insurancePayAmount;

    /**
     * 乙类自付金额（已包含在个人自付中），单位：元
     */
    private BigDecimal selfPayAmount;

    /**
     * 个人自付金额（患者实付），单位：元
     */
    private BigDecimal patientPayAmount;

    /**
     * 支付二维码（data:image/png;base64,...）
     */
    private String payQrCode;

    /**
     * 支付二维码内容
     */
    private String payQrContent;
}

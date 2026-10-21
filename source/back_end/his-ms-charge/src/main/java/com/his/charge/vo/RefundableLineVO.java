package com.his.charge.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 账单里「还能整条退」的一行（退费申请的候选清单）。
 */
@Data
public class RefundableLineVO {

    /**
     * 来源记账行ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long feeRecordId;

    /**
     * 费用项目类型（字典 {@code his_charge_item_type}），退费类型按它圈范围
     */
    private Integer itemType;

    private String itemTypeText;

    /**
     * 来源单据类型（字典 {@code his_fee_source_type}：1挂号 2处方 3检查申请 4检验申请…），
     * 前端圈选时按它 + {@link #sourceNo} 告诉收费员"这一行来自哪张单"
     */
    private Integer sourceType;

    /**
     * 来源单据号（处方号/申请单号；手工补记账为空）
     */
    private String sourceNo;

    /**
     * 项目编码
     */
    private String itemCode;

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 规格
     */
    private String specification;

    /**
     * 单位
     */
    private String unit;

    /**
     * 单价
     */
    private BigDecimal price;

    /**
     * 当前剩余可退数量（原数量加上名下未退场的红冲负行数量：部分退过的行只剩一半可退）
     */
    private BigDecimal quantity;

    /**
     * 当前剩余可退金额（原行金额 + 名下未退场的红冲负行）
     */
    private BigDecimal amount;
}

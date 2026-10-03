package com.his.charge.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 账单里「还能整条退」的一行（退费申请的候选清单）。
 *
 * <p>退费申请单只有<b>类型 + 金额</b>两个旋钮，没有"勾哪几行"，所以金额必须由这些行的
 * 整条金额前缀和拼出来 —— 这个列表就是那道边界，也是窗口报错时能报出「还能退成哪几个数」的依据。
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
     * 项目编码（快照）
     */
    private String itemCode;

    /**
     * 项目名称（快照）
     */
    private String itemName;

    /**
     * 规格（快照）
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
     * 当前剩余数量（原数量减去已红冲数量）
     */
    private BigDecimal quantity;

    /**
     * 当前剩余可退金额（原行金额 + 名下未退场的红冲负行）
     */
    private BigDecimal amount;
}

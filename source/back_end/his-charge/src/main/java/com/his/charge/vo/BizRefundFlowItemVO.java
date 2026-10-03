package com.his.charge.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 退费流水明细项（投影自账单行结算账单行）。
 *
 * <p>退费是整单退，被退的项目即账单明细，所以明细直接取账单行即可；
 * {@code refundQuantity}/{@code refundAmount} 映射账单行的数量/金额。
 */
@Data
public class BizRefundFlowItemVO {

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 规格
     */
    private String specification;

    private BigDecimal refundQuantity;

    /**
     * 单位
     */
    private String unit;

    /**
     * 单价
     */
    private BigDecimal price;

    /**
     * 退费金额
     */
    private BigDecimal refundAmount;

    /**
     * 账单行无独立来源单号列，留空（历史旧退费明细的来源编号仅为快照冗余）
     */
    private String sourceNo;
}

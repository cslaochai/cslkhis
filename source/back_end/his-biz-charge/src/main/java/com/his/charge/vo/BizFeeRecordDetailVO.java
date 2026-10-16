package com.his.charge.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 记账行详情（红冲链成对回显）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BizFeeRecordDetailVO extends BizFeeRecordVO {

    /**
     * 已被冲减金额（负数或 0）
     */
    private BigDecimal reversedAmount;

    /**
     * 冲减后净额 = amount + reversedAmount
     */
    private BigDecimal remainingAmount;

    /**
     * 本行冲了谁（负行才有值）
     */
    private BizFeeRecordVO reverseOf;

    /**
     * 冲本行的所有负行（原行才有值）
     */
    private List<BizFeeRecordVO> reverseRows;
}

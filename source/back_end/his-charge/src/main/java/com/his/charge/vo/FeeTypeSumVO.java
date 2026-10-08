package com.his.charge.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 某次就诊按项目类型聚合的应收净额（L1 读出口）。
 */
@Data
public class FeeTypeSumVO {

    /**
     * 项目类型（字典 {@code his_charge_item_type}）
     */
    private Integer itemType;

    /**
     * 该类型净额（含红冲负行，已全额红冲的行不计）
     */
    private BigDecimal amount;
}

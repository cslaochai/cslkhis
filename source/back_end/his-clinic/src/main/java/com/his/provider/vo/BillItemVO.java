package com.his.provider.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 结算账单明细行（读侧契约，供费用解释等跨模块读取）。
 */
@Data
public class BillItemVO {

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 项目类型（字典 his_charge_item_type）
     */
    private Integer itemType;

    /**
     * 医保目录类别：0-自费 1-甲类 2-乙类 3-丙类
     */
    private Integer catalogType;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 医保统筹分摊
     */
    private BigDecimal poolAmount;

    /**
     * 医保个人账户分摊
     */
    private BigDecimal accountAmount;

    /**
     * 个人自付分摊
     */
    private BigDecimal selfAmount;
}

package com.his.provider.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 账单费用构成：一张账单按费用类别（his_charge_item_type）汇总的行数与金额。
 * 给账单列表的"费用构成"列用，前端再按字典把 itemType 翻成"挂号费/西药/检查/检验/治疗/耗材材料"等文案。
 */
@Data
public class BillFeeTypeSumVO {

    /**
     * 账单ID（对应 biz_settlement_bill.id）
     */
    private Long billId;

    /**
     * 费用类别（字典 his_charge_item_type）
     */
    private Integer itemType;

    /**
     * 该类费用行数
     */
    private Integer count;

    /**
     * 该类费用金额合计（应收口径，含红冲负行已在 SQL 侧排除）
     */
    private BigDecimal amount;
}

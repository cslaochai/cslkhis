package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 日清单 / 结算试算里的单条费用行（一行 = 一条 L1 记账行，红冲负行同样是一行）。
 *
 * <p>金额直接就是记账行金额（负行为负），不做任何"取绝对值再猜方向"的处理：
 * 清单合计永远等于 SUM(记账行)，也就是这次住院的应收净额。
 */
@Data
public class DailyBillItemVO implements Serializable {

    /**
     * 记账行单号（L1 费用记账流水的费用编号）
     */
    private String feeNo;

    /**
     * 项目类型：1-挂号费 2-西药 3-中成药 4-中药饮片 5-检查 6-检验 7-治疗
     */
    private Integer itemType;

    /**
     * 项目类型文案
     */
    private String itemTypeName;

    /**
     * 项目/药品编码
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
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 单价
     */
    private BigDecimal price;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 发生时间（明细的创建时间）
     */
    private String occurTime;

    /**
     * 来源单号（医嘱号等，四核对的锚点）
     */
    private String sourceNo;
}

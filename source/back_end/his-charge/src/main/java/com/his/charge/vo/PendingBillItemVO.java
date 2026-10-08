package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 患者端待缴账单的摊行明细（PendingBillVO#getDetails() 的元素）。
 */
@Data
public class PendingBillItemVO implements Serializable {

    /**
     * 费用项目名称
     */
    private String itemName;

    /**
     * 行金额（元）
     */
    private BigDecimal amount;

    /**
     * 开单科室名称
     */
    private String deptName;

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
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 医保拆分：统筹支付（元）。行级快照，原样透出
     */
    private BigDecimal poolAmount;

    /**
     * 医保拆分：个人账户支付（元）。行级快照，原样透出
     */
    private BigDecimal accountAmount;

    /**
     * 医保拆分：自费（元）。行级快照，原样透出
     */
    private BigDecimal selfAmount;

    /**
     * 医保目录类别（0-自费 1-甲类 2-乙类 3-丙类）
     */
    private Integer catalogType;
}
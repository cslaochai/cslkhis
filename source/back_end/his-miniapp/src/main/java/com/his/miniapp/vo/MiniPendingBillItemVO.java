package com.his.miniapp.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 患者端待缴账单明细行。
 */
@Data
public class MiniPendingBillItemVO implements Serializable {

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 金额（元）
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
     * 行级医保统筹（元）
     */
    private BigDecimal poolAmount;

    /**
     * 行级医保个账（元）
     */
    private BigDecimal accountAmount;

    /**
     * 行级个人自付（元）
     */
    private BigDecimal selfAmount;

    /**
     * 医保目录类别（0-自费 1-甲类 2-乙类 3-丙类）
     */
    private Integer catalogType;

    /**
     * 医保目录类别文本（自费/甲类/乙类/丙类），后端算好，避免前端各写一套口径
     */
    private String catalogTypeText;
}

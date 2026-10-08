package com.his.patient.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 住院医嘱明细项（一个组套里的一条项目）。
 */
@Data
public class InpatientOrderItemDTO implements Serializable {

    /**
     * 医嘱类别：1-药品 2-检查 3-检验 4-治疗 5-护理 6-手术 7-输血 8-监护 9-其他 10-临床营养（必填）
     */
    private Integer orderClass;

    /**
     * 项目编码（药品/检查/检验字典码）
     */
    private String itemCode;

    /**
     * 项目名称（必填）
     */
    private String itemName;

    /**
     * 规格
     */
    private String spec;

    /**
     * 单位
     */
    private String unit;

    /**
     * 单次剂量
     */
    private BigDecimal dosage;

    /**
     * 剂量单位
     */
    private String dosageUnit;

    /**
     * 给药途径（口服/静滴/肌注…）
     */
    private String route;

    /**
     * 频次（qd/bid/tid/q8h…）——本期仅展示与执行参考，不据此做时点排程
     */
    private String frequency;

    /**
     * 本次执行数量（默认 1）
     */
    private BigDecimal quantity;

    /**
     * 单价（元，开立时快照）
     */
    private BigDecimal price;
}

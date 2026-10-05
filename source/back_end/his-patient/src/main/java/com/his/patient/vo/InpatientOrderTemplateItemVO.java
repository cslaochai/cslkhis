package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 医嘱模板明细行出参。
 *
 * <p>单价明确是**参考价**：套用时前端按 {@code itemCode} 回查字典现价覆盖它，
 * 回查不到（字典已下架 / 手输类项目）才沿用此值并提示医生确认。
 */
@Data
public class InpatientOrderTemplateItemVO implements Serializable {

    /**
     * 明细ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 模板主表ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

    /**
     * 排序
     */
    private Integer sortNo;

    /**
     * 医嘱类别
     */
    private Integer orderClass;

    private String orderClassText;

    /**
     * 项目编码（药品/检查/检验字典码，套用时按它回查现价与下拉选中态）
     */
    private String itemCode;

    /**
     * 项目名称
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
     * 给药途径
     */
    private String route;

    /**
     * 频次
     */
    private String frequency;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 录入时单价
     */
    private BigDecimal price;
}

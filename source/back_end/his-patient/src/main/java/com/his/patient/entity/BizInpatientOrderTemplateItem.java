package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 住院医嘱模板明细（sql/103）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_inpatient_order_template_item")
public class BizInpatientOrderTemplateItem extends BaseEntity implements Serializable {

    /**
     * 模板主表ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

    /**
     * 排序（服务端按提交顺序写，套用时保持医生录入顺序）
     */
    private Integer sortNo;

    /**
     * 医嘱类别：1-药品 2-检查 3-检验 4-治疗 5-护理 6-手术 7-输血 8-监护 9-其他 10-临床营养
     */
    private Integer orderClass;

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

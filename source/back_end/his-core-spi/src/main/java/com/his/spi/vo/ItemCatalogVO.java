package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 项目目录项（跨模块选取用）：体检套餐明细、开单候选等只需要「编码/名称/来源/类型/价格/参考值」。
 *
 * <p>与 {@link LabItemVO} 分开：那个是开单快照（带科室与标本类型），
 * 这里带参考值与单位，两边字段宽度不同，不并成一个类。</p>
 */
@Data
public class ItemCatalogVO implements Serializable {

    /**
     * 目录项目 ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 项目编码
     */
    private String itemCode;

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 项目来源（1-检验项目 2-检查项目 3-一般检查）
     */
    private Integer itemSource;

    /**
     * 来源字典自身的项目类型（语义随 itemSource：检验 1-血液… / 检查 1-放射… / 一般 1-注射…）
     */
    private Integer itemType;

    /**
     * 项目单价（元）
     */
    private BigDecimal price;

    /**
     * 参考范围（检验项目取「参考值 + 单位」，检查/一般项目通常为空）
     */
    private String refStandard;

    /**
     * 单位（仅检验项目有）
     */
    private String unit;
}

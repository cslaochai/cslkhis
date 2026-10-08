package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 胶片规格价目（胶片规格价目，sql/138）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_film_spec")
public class BizFilmSpec extends BaseEntity {

    /**
     * 规格编码（唯一）
     */
    private String specCode;

    /**
     * 规格名称（如 14×17英寸激光胶片）
     */
    private String specName;

    /**
     * 单价（元/张）
     */
    private BigDecimal unitPrice;

    /**
     * 计价单位（默认张）
     */
    private String unit;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}

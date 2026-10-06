package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 胶片规格价目（胶片规格价目，sql/138）。
 *
 * <p>规格的**唯一口径就是这张表**：下拉选项、单价、列表里的规格名全从这里出，
 * 刻意为它不建字典数据字典 —— 字典只能存 label，一旦「字典里的 14×17」
 * 和「价目表里的 14×17」改了其中一个，列表就会把同一件事显示成两个名字。
 *
 * <p>uk_spec_code 不含 del_flag → 删除走物理删（配置数据没有留档价值，
 * 软删留下的行会一直占着编码，同编码规格再也建不出来）。
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

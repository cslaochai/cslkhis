package com.his.patient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 医嘱基础字典分页查询入参（管理页按「途径 / 频次 / 剂量单位」分 Tab 查）。
 *
 * <p>{@code dictType} 必填：三种字典共表，不指定类型就会把途径和频次混在一页里。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderDictQueryPageDTO extends PageParam implements Serializable {

    /**
     * 字典类型：his_order_route / his_order_freq / his_dose_unit（必填）
     */
    private String dictType;

    /**
     * 名称或值模糊
     */
    private String keyword;

    /**
     * 状态：1-启用 0-停用（不传=全部）
     */
    private Integer status;
}

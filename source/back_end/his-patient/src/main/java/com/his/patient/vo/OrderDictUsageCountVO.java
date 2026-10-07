package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 医嘱字典值使用量计数行（对应 {@code SysOrderDictDataMapper.countOrderUsage}）。
 *
 * <p>SQL 里被统计的列是**动态列名**（route / frequency / dosage_unit 三选一，由
 * {@code OrderDictTypes.orderColumn()} 传入），所以本类只能有一个泛化的
 * {@code dictValue} 字段 —— 它装的是哪一列的字典值由入参 column 决定，不是固定列。
 * 这个"动态"是查询维度，不是数据结构不确定，所以仍然用有类型的 VO 承接。
 *
 * <p>字段不用 {@code v}/{@code c}：字典值可能是 route 也可能是 dosage_unit，
 * 光看字段名根本判断不出装的是什么。
 */
@Data
public class OrderDictUsageCountVO implements Serializable {

    /**
     * 字典值（内容取决于动态列：给药途径 / 频次 / 剂量单位）
     */
    private String dictValue;

    /**
     * 引用该字典值的医嘱条数
     */
    private Long cnt;
}

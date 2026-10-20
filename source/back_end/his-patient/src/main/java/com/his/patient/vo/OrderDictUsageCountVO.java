package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 医嘱字典值使用量计数行（对应 SysOrderDictDataMapper.countOrderUsage）。
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

package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * MDC/ADRG 主诊断 ICD 前缀分布（实时从 biz_drg_set 聚合，仅用于前端目录页展示提示，不得用于入组或业务逻辑）。
 *
 * <p>每个条目是「ICD-10 前3位 + 该前缀下的编码数量」，前端可把连续前缀合并成 G00-G99 这样的范围提示。
 *
 * @see com.his.charge.controller.DrgSimController#mdcDiagPrefixes(String, String)
 */
@Data
public class DrgDiagPrefixVO implements Serializable {

    /**
     * ICD-10 编码前3位（如 G04、I61）
     */
    private String prefix;

    /**
     * 该前缀下纳入本 DI 集合的精确编码数量
     */
    private Integer count;
}

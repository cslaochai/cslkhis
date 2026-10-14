package com.mis.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 三大目录统计（按类别 / 支付类别聚合）
 */
@Data
public class CatalogStatsVO implements Serializable {

    /** 按目录类别：类别 -> 数量 */
    private Map<String, Long> byCat;
    /** 按支付类别：甲/乙/丙 -> 数量 */
    private Map<String, Long> byClass;
    private long total;
    private long disabled;
}

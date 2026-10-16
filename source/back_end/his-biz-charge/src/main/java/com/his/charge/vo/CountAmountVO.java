package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 「笔数 + 金额」聚合行：日结/班结里所有单值聚合的共同形状。
 */
@Data
public class CountAmountVO implements Serializable {

    /**
     * 笔数（{@code COUNT(*)}；MySQL BIGINT）
     */
    private Long cnt;

    /**
     * 金额合计（元）。SQL 已用 {@code COALESCE(..., 0)} 兜底，不会为 null
     */
    private BigDecimal amount;
}
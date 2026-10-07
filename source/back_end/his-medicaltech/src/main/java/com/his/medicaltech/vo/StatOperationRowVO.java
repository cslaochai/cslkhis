package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 出院队列手术台次（{@code StatReportAggMapper#operationStats}）。
 *
 * <p>注意统计单位是<b>手术台次</b>不是"人次"：同一次住院做两台手术计 2。
 * 关联 biz_admission 用的是出院队列口径，与本类其余聚合保持同一批病例。
 */
@Data
public class StatOperationRowVO implements Serializable {

    /**
     * 手术台次
     */
    private Long operationCount;

    /**
     * 三级及以上手术台次
     */
    private Long level3upCount;
}
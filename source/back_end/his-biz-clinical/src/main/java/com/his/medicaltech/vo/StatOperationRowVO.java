package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 出院队列手术台次（StatReportAggMapper#operationStats）。
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
package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 手术级别构成（StatReportAggMapper#operationLevelDist 一行）。
 */
@Data
public class StatOperationLevelRowVO implements Serializable {

    /**
     * 手术级别（0-未录级别 1-一级 2-二级 3-三级 4-四级）
     */
    private Integer level;

    /**
     * 该级别台次
     */
    private Long cnt;
}
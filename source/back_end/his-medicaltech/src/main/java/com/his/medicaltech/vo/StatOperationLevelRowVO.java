package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 手术级别构成（{@code StatReportAggMapper#operationLevelDist} 一行）。
 *
 * <p>{@code level} 是 0 而不是 null 表示"未录级别" —— 手术台次必须都落在某一档里，
 * 漏掉未录的那批会让各级台次之和对不上总台次，评审一眼就能看出数字对不上。
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
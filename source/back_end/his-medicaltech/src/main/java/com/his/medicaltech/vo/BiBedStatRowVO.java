package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 床位构成（BiMapper#bedStat）。
 */
@Data
public class BiBedStatRowVO implements Serializable {

    /**
     * 维修床位数
     */
    private Long repair;

    /**
     * 占用床位数
     */
    private Long occupied;

    /**
     * 床位总数
     */
    private Long total;
}
package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 床位构成（{@code BiMapper#bedStat}）。
 *
 * <p>"可用床位"不在这里算：它是 {@code total - repair}，属业务判断，
 * 由服务层收口（BI 总览与国考指标都要用，口径必须一致）。
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
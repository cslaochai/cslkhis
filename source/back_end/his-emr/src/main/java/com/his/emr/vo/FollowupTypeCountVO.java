package com.his.emr.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 随访看板：按随访方式的分布。
 */
@Data
public class FollowupTypeCountVO implements Serializable {

    /**
     * 随访方式码值（1-复诊提醒 2-慢病随访 3-用药指导 4-术后随访）
     */
    private Integer followupType;

    /**
     * 条数
     */
    private Long cnt;
}
package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 近 30 日主手术级别统计一行（BiMapper#operationLevel30d）。
 */
@Data
public class BiOpLevelRowVO implements Serializable {

    /** 主手术总台次 */
    private Long total;

    /** 三、四级手术台次 */
    private Long level34;
}

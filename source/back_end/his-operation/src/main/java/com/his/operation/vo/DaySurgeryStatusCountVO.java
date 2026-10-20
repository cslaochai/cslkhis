package com.his.operation.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 日间手术按状态分组的登记单数（BizDaySurgeryApplyMapper#countByStatus 的返回行）。
 */
@Data
public class DaySurgeryStatusCountVO implements Serializable {

    /**
     * 登记单状态（1-待评估 2-评估通过 3-已排台 4-术后观察 5-已离院 6-已取消 7-已转住院）
     */
    private Integer status;

    /**
     * 该状态的登记单数
     */
    private Long cnt;
}

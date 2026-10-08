package com.his.operation.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 日间手术统计（服务端 group by 出，不让前端数当前页）。
 */
@Data
public class DaySurgeryStatVO implements Serializable {

    /**
     * 总条数
     */
    private Long total;

    private Long waitEvalCount;

    private Long evalPassedCount;

    private Long arrangedCount;

    private Long observingCount;

    private Long dischargedCount;

    private Long canceledCount;

    private Long transferredCount;

    /**
     * 术后滞留超期数（服务端判，不落库）
     */
    private Long overdueCount;

    /**
     * 应随访未随访数（离院超 24h 且随访次数 0）
     */
    private Long followOverdueCount;

    /**
     * 非计划再入院数（离院方式=3）
     */
    private Long readmitCount;

    /**
     * 48h 内按时离院率（%，已出院中 leave_type=1 占比）
     */
    private BigDecimal onTimeLeaveRate;

    /**
     * 术式分布 TOP（按登记数倒序前 10）
     */
    private List<DaySurgeryItemCountVO> byItemTop;
}

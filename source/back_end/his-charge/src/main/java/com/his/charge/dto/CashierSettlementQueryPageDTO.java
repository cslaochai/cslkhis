package com.his.charge.dto;

import lombok.Data;

/**
 * 交班单分页查询入参。
 */
@Data
public class CashierSettlementQueryPageDTO {

    /**
     * 收费员工号
     */
    private Long cashierId;

    /**
     * 状态（1-已交班待日结 2-已日结 3-已审核）
     */
    private Integer settleStatus;

    /**
     * 统计区间止日期起（yyyy-MM-dd，按 period_end 过滤）
     */
    private String dateStart;

    /**
     * 统计区间止日期止
     */
    private String dateEnd;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;
}

package com.his.charge.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 日结单分页查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DaySettlementQueryPageDTO extends PageParam {

    /**
     * 对账结论（1-已平 2-有差异）
     */
    private Integer reconcileStatus;

    /**
     * 状态（1-待审核 2-已审核）
     */
    private Integer settleStatus;

    /**
     * 日期起（yyyy-MM-dd）
     */
    private String dateStart;

    /**
     * 日期止
     */
    private String dateEnd;
}

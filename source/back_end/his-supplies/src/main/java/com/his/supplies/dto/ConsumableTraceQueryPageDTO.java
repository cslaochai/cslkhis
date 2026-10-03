package com.his.supplies.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 高值耗材溯源台账分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConsumableTraceQueryPageDTO extends PageParam {
    /** 关键字（追溯码/UDI/患者姓名/患者编号/耗材名称） */
    private String keyword;
    /** 耗材ID */
    private Long consumableId;
    /** 患者ID（从患者查到用过哪些码） */
    private Long patientId;
    /** 计费状态（0-未计费 1-已计费 2-计费失败） */
    private Integer chargeStatus;
    /** 记录状态（1-使用中 2-已作废） */
    private Integer status;
}

package com.his.charge.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 退费流水分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RefundFlowQueryPageDTO extends PageParam {

    /**
     * 关键字：退费单号 / 原收费单号 / 患者姓名模糊匹配
     */
    private String keyword;

    /**
     * 流水来源（字典 his_refund_flow_source：0-存量铺底 1-退费申请执行 2-收费处直退 3-退号联动）
     */
    private Integer flowSource;

    /**
     * 退费方式（字典 his_refund_method：1-原路退回 2-现金退回 3-余额退回）
     */
    private Integer refundMethod;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 来源退费申请ID
     */
    private Long applyId;
}

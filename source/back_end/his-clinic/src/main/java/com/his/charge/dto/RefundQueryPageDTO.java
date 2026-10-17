package com.his.charge.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 退费申请查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RefundQueryPageDTO extends PageParam {

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 申请状态（1-待审核 2-审核通过 3-审核驳回 4-已退费 5-已作废）
     */
    private Integer applyStatus;

    /**
     * 关键字：退费申请号 / 原收费单号 / 患者姓名模糊匹配
     */
    private String keyword;
}

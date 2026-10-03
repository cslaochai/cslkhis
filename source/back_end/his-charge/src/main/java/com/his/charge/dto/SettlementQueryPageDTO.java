package com.his.charge.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 医保结算清单查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SettlementQueryPageDTO extends PageParam {

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 患者姓名（模糊匹配）
     */
    private String patientName;

    /**
     * 清单状态（1-待结算 2-已结算 3-已上传 4-已审核 5-已作废）
     */
    private Integer settlementStatus;
}

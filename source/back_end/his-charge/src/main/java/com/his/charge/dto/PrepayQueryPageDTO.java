package com.his.charge.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 预交金流水分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PrepayQueryPageDTO extends PageParam {

    /**
     * 入院ID
     */
    private Long admissionId;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 流水类型：1-充值 2-退款（不传查全部）
     */
    private Integer prepayType;
}

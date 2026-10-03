package com.his.charge.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 预交金流水分页查询入参
 *
 * <p>查询一律 GET，分页 DTO 必须继承 {@link PageParam} 且 {@code callSuper = true}
 * （否则 admissionId 等条件不参与 {@code equals}，MyBatis 的缓存键会串）。
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

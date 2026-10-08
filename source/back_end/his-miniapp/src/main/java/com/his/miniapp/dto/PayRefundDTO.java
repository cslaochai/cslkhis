package com.his.miniapp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 患者端退款入参（退号退费等）。
 */
@Data
public class PayRefundDTO {

    /**
     * 业务类型（1-门诊缴费 2-挂号费 3-住院押金）
     */
    @NotNull(message = "业务类型不能为空")
    private Integer bizType;

    /**
     * 业务单ID（收费单ID/挂号单ID/入院ID）
     */
    @NotNull(message = "业务单ID不能为空")
    private Long bizId;

    /**
     * 退款原因
     */
    private String reason;
}

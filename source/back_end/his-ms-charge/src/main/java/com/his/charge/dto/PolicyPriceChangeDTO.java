package com.his.charge.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 政策价调价入参（价格管理页「政策价」页签）。
 *
 * <p>全额必填；加收额可空 = 不动加收列（仅门诊副主任及以上档有加收结构）。
 * 调价写 sys_price_change_history（itemType=POLICY）留痕。
 */
@Data
public class PolicyPriceChangeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 价目行主键（sys_charge_policy_price.id） */
    @NotNull(message = "价目行不能为空")
    private Long priceId;

    /** 新全额（元） */
    @NotNull(message = "新全额不能为空")
    @DecimalMin(value = "0", message = "新全额不能为负数")
    private BigDecimal amount;

    /** 新加收额（元）：null = 不动加收列；门诊副主任及以上档调全额时建议一起核 */
    @DecimalMin(value = "0", message = "加收额不能为负数")
    private BigDecimal surcharge;

    /** 调价原因（必填，进调价历史） */
    @NotNull(message = "调价原因不能为空")
    private String reason;
}

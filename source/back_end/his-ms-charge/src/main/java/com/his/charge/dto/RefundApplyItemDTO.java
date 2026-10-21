package com.his.charge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;


/**
 * 退费申请明细行入参：勾了哪条记账行、退多少数量。
 *
 * <p>{@code refundQuantity} 传空 = 该行整行退（剩余量全退）；给了数量 = 数量级部分退
 * （共 5 盒退 2 盒）。服务端按该行剩余可退数量兜底校验，不接受"超过剩余量"。
 */
@Data
public class RefundApplyItemDTO {

    /**
     * 记账行ID（必须落在该账单 + 该退费类型的候选清单内）
     */
    @NotNull(message = "缺少记账行")
    private Long feeRecordId;

    /**
     * 本次退费数量；空 = 整行退
     */
    private BigDecimal refundQuantity;
}

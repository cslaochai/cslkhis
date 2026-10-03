package com.his.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 作废医嘱入参。
 *
 * <p>**只允许作废「待校对」的医嘱**（医生刚开错、护士还没看到）。
 * 已校对/已执行的医嘱只能"停止"—— 否则相当于把护理已经做过的事从记录里抹掉，
 * 而执行记录是飞检要件，抹不掉也不该抹。
 */
@Data
public class InpatientOrderCancelDTO implements Serializable {

    /**
     * 医嘱ID（必填）
     */
    @NotNull(message = "医嘱ID不能为空")
    private Long orderId;

    /**
     * 作废原因（必填）
     */
    @NotBlank(message = "作废原因不能为空")
    private String cancelReason;
}

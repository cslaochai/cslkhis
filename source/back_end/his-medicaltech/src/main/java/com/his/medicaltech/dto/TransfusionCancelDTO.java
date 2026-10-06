package com.his.medicaltech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 取消输血申请入参。
 *
 * <p>仅「待配血 / 已配血 / 已发血」可取消。<b>输注中（3）与已完成（4）不可取消</b>：
 * 血已经进入患者体内，取消它是销毁证据；已完成的血还写进了病历与首页标志，
 * 抹掉会给"首页说输过血、病历里查不到"制造矛盾。
 */
@Data
public class TransfusionCancelDTO implements Serializable {

    /**
     * 输血申请单ID（必填）
     */
    @NotNull(message = "输血申请单ID不能为空")
    private Long applyId;

    /**
     * 取消原因（必填：停止用血是一个临床决定，必须有人负责、有理由）
     */
    @NotBlank(message = "取消原因不能为空（停止用血是一个临床决定，必须有人负责）")
    private String cancelReason;
}

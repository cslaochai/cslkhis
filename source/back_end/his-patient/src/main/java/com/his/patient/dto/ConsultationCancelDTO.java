package com.his.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 会诊取消入参。
 *
 * <p>只允许取消「待应答」的申请：会诊方一旦接诊，这条记录就必须走「完成」——
 * 用取消把已发生的临床行为抹掉，等于销毁证据。
 */
@Data
public class ConsultationCancelDTO implements Serializable {

    /**
     * 会诊ID（必填）
     */
    @NotNull(message = "会诊ID不能为空")
    private Long consultationId;

    /**
     * 取消原因（必填）
     */
    @NotBlank(message = "取消原因不能为空（取消是一个临床决定，必须有人负责）")
    private String cancelReason;
}

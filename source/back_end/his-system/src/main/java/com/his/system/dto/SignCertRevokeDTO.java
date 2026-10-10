package com.his.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 吊销证书入参。吊销必须写理由，且吊销后**不删行**。
 */
@Data
public class SignCertRevokeDTO {

    @NotNull(message = "证书ID不能为空")
    private Long certId;

    /**
     * 吊销原因（必填）
     */
    private String reason;
}

package com.his.spi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * CA 在线申请单操作入参：提交/重试或刷新状态，按证书行定位。
 */
@Data
public class SignCertCaDTO {

    /**
     * 证书ID（生成密钥对时返回的那一行，状态须为「待CA签发」）
     */
    @NotNull(message = "证书ID不能为空")
    private Long certId;
}

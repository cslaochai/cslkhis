package com.his.spi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 回填 CA 签发证书入参（CA 线下签发流程的第二步）。
 *
 * <p>管理员拿到 CA 线下签发回来的证书后，把证书 PEM 贴回系统；系统校验证书公钥与
 * 本地密钥对一致后写入证书链与 CA 回执信息，证书状态由「待CA签发」转为「有效」。
 */
@Data
public class SignCertBindDTO {

    /**
     * 待回填的证书ID（生成密钥对时返回的那一行）
     */
    @NotNull(message = "证书ID不能为空")
    private Long certId;

    /**
     * CA 签发回来的证书 PEM（含证书链，-----BEGIN CERTIFICATE----- 开头）
     */
    @NotBlank(message = "证书内容不能为空")
    private String certPem;

    /**
     * 备注
     */
    private String remark;
}

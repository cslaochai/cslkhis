package com.his.system.dto;

import lombok.Data;

/**
 * 修改密码入参
 */
@Data
public class ChangePasswordDTO {

    /**
     * 旧密码：SM2 公钥加密后的十六进制密文（C1C3C2，不带 04 前缀）。
     *
     * <p>公钥从 {@code GET /auth/publicKey} 取，与登录共用一对密钥；明文一律拒收。
     */
    private String oldPassword;

    /**
     * 新密码：同上，SM2 密文入参，明文一律拒收。
     */
    private String newPassword;
}

package com.his.system.dto;

import lombok.Data;

/**
 * 用户登录入参
 */
@Data
public class LoginRequestDTO {

    /**
     * 登录账号
     */
    private String username;

    /**
     * 登录密码：SM2 公钥加密后的十六进制密文（C1C3C2，不带 04 前缀）。
     *
     * <p>公钥从 {@code GET /auth/publicKey} 取。<b>明文一律拒收</b> —— 留明文口子等于这套加密没做。
     */
    private String password;

    /**
     * 选中的角色编码（空字符串或null表示选择"其他"）
     */
    private String roleCode;
}

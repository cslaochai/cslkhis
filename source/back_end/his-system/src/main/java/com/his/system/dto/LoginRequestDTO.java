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
     * 登录密码
     */
    private String password;

    /**
     * 选中的角色编码（空字符串或null表示选择"其他"）
     */
    private String roleCode;
}

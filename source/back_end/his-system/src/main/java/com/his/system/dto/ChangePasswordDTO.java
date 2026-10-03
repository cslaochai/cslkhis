package com.his.system.dto;

import lombok.Data;

/**
 * 修改密码入参
 */
@Data
public class ChangePasswordDTO {

    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;
}

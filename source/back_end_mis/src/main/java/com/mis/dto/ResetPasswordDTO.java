package com.mis.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 重置密码入参（兼容 HIS：只传 userId，密码缺省默认 123456）
 */
@Data
public class ResetPasswordDTO implements Serializable {

    /** MSS 口径：用户ID */
    private Long id;

    /** HIS 口径：用户ID */
    private Long userId;

    /** 新密码（缺省时默认 123456） */
    private String password;

    public Long resolveId() {
        return id != null ? id : userId;
    }
}

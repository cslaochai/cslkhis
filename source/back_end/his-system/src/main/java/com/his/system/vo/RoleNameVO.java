package com.his.system.vo;

import lombok.Data;

/**
 * 角色编码与名称的对照项：/auth/info 随用户信息一起返回，
 */
@Data
public class RoleNameVO {

    /**
     * 角色编码（唯一）
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;
}

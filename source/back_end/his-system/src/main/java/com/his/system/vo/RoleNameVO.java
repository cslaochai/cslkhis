package com.his.system.vo;

import lombok.Data;

/**
 * 角色编码与名称的对照项：{@code /auth/info} 随用户信息一起返回，
 * 供顶栏显示「当前角色」和切换角色弹窗使用，省掉一次全院角色字典请求。
 */
@Data
public class RoleNameVO {

    /** 角色编码（唯一） */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;
}

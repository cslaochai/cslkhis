package com.his.system.vo;

import lombok.Data;

import java.util.List;

/**
 * 当前用户角色列表出参
 */
@Data
public class UserRolesVO {

    /**
     * 当前角色编码
     */
    private String currentRole;

    /**
     * 用户拥有的全部角色编码
     */
    private List<String> roles;
}

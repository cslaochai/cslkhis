package com.mis.config;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 当前登录用户（轻量，从 token 解析 + /auth/info 时补全角色权限）
 */
@Data
public class LoginUser implements Serializable {

    private Long userId;
    private String username;
    private String realName;
    private Long empId;
    private String empName;
    /** 当前角色编码（token 携带） */
    private String roleCode;
    /** 当前角色名称 */
    private String roleName;
    private Long deptId;
    private String deptName;
    /** 该用户全部角色（用于 /auth/info 展示） */
    private List<String> roleCodes;
    /** 当前角色的权限码集合 */
    private List<String> permissions;
}

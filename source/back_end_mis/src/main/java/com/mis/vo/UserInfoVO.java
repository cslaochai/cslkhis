package com.mis.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 当前登录用户信息出参（/auth/info）
 */
@Data
public class UserInfoVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private String username;
    private String realName;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long empId;
    private String empName;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
    private String deptName;
    private String avatar;

    /** 当前角色编码与名称（token 携带，登录时取主岗位角色） */
    private String roleCode;
    private String roleName;

    /** 该用户全部角色编码与名称 */
    private List<String> roleCodes;
    private List<String> roleNames;

    /** 当前角色可见菜单的权限码集合 */
    private List<String> permissions;
}

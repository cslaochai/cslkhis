package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

/**
 * 用户信息返回对象
 */
@Data
public class UserLoginVO {

    /**
     * 用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 当前主科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 当前主科室名称
     */
    private String deptName;

    /**
     * 角色列表
     */
    private List<String> roles;

    /**
     * 角色编码 → 名称对照：顶栏显示当前角色、切换角色弹窗都要用，
     * 随本接口一次性返回，避免每个岗位进首页都去请求全院角色字典（那是管理岗接口）。
     */
    private List<RoleNameVO> roleNames;

    /**
     * 权限列表
     */
    private List<String> permissions;

    /**
     * 当前激活的角色
     */
    private String currentRole;
}
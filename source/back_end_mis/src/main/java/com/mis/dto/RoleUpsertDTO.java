package com.mis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 角色新增/修改入参（menuIds 为角色授权的菜单 id 集合，整单替换）
 */
@Data
public class RoleUpsertDTO implements Serializable {

    private Long id;

    /** 角色编码（新增时缺省由后端自动生成 ROLE_ 前缀） */
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    /** 角色类型（1-系统角色 2-自定义角色） */
    private Integer roleType;

    /** 数据权限范围（1-全部数据 2-自定义数据 3-本部门数据 5-仅本人数据） */
    private Integer dataScope;

    private Integer sortOrder;

    /** 状态（0-停用 1-启用） */
    private Integer status;

    private String remark;

    /** 授权的菜单 id 集合 */
    private List<Long> menuIds;
}

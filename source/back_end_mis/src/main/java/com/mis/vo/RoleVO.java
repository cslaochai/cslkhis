package com.mis.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 角色出参（编辑回显带 menuIds）
 */
@Data
public class RoleVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String roleCode;
    private String roleName;
    /** 角色类型（1-系统角色 2-自定义角色） */
    private Integer roleType;
    /** 数据权限范围（1-全部数据 2-自定义数据 3-本部门数据 5-仅本人数据） */
    private Integer dataScope;
    private Integer sortOrder;
    /** 状态（0-停用 1-启用） */
    private Integer status;
    private String remark;

    /** 已授权的菜单 id 集合 */
    private List<Long> menuIds;
}

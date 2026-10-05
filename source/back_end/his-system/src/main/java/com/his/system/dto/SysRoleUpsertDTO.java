package com.his.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 角色新增/修改入参
 */
@Data
public class SysRoleUpsertDTO {

    /**
     * 角色ID，新增时为空，修改时必填
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 角色编码，唯一（新增时由系统自动生成，可不传）
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色类型：0-自定义角色 1-系统角色（系统角色不允许修改类型/编码、不允许删除）
     */
    private Integer roleType;

    /**
     * 数据权限范围（1-全部数据 2-自定义数据 3-本部门数据 4-本部门及以下 5-仅本人数据）
     */
    private Integer dataScope;

    /**
     * 排序号，越小越靠前
     */
    private Integer sortOrder;

    /**
     * 状态：0-停用 1-启用
     */
    private Integer status;
}

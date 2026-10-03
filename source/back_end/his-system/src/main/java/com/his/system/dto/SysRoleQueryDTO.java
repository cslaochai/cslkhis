package com.his.system.dto;

import lombok.Data;

/**
 * 角色查询入参
 */
@Data
public class SysRoleQueryDTO {

    /**
     * 角色名称，模糊匹配
     */
    private String roleName;

    /**
     * 状态：0-停用 1-启用
     */
    private Integer status;

}

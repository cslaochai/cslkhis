package com.mis.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 角色菜单授权入参（HIS：/system/role/saveRoleMenu，整表替换）
 */
@Data
public class RoleMenuSaveDTO implements Serializable {

    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    private List<Long> menuIds;
}

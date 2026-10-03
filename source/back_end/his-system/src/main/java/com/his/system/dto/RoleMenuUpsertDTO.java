package com.his.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 角色菜单权限保存入参
 *
 * <p>整表替换语义：以传入的 menuIds 作为该角色最终的菜单权限集合，
 * 为空表示收回全部菜单（系统管理员角色除外，后端会拒绝）。</p>
 */
@Data
public class RoleMenuUpsertDTO {

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roleId;

    /**
     * 菜单ID集合（保存后该角色只能看到这些菜单）
     */
    private List<Long> menuIds;
}

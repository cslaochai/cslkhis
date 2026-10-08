package com.his.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 菜单新增/修改入参
 */
@Data
public class MenuUpsertDTO {

    /**
     * 菜单ID，新增时为空，修改时必填
     */
    private Long id;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 父菜单ID，顶级菜单为0
     */
    private Long parentId;

    /**
     * 排序号，越小越靠前
     */
    private Integer sortOrder;

    /**
     * 菜单类型：1-目录 2-菜单 3-按钮
     */
    private Integer menuType;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 菜单唯一标识（权限标识键）
     */
    private String menuKey;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 权限标识，如：system:user:list
     */
    private String permission;

    /**
     * 是否外链：0-否 1-是
     */
    private Integer isFrame;

    /**
     * 是否缓存：0-否 1-是
     */
    private Integer isCache;

    /**
     * 是否显示：0-隐藏 1-显示
     */
    private Integer isVisible;

    /**
     * 状态：0-停用 1-启用
     */
    private Integer status;
}

package com.mis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 菜单新增/修改入参
 */
@Data
public class MenuUpsertDTO implements Serializable {

    private Long id;

    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    private Long parentId;

    private Integer sortOrder;

    /** 菜单类型（1-目录 2-菜单 3-按钮） */
    private Integer menuType;

    private String path;

    private String component;

    private String menuKey;

    private String icon;

    private String permission;

    /** 是否外链（0-否 1-是） */
    private Integer isFrame;

    /** 是否缓存（0-否 1-是） */
    private Integer isCache;

    /** 是否可见（0-隐藏 1-显示） */
    private Integer isVisible;

    /** 状态（0-停用 1-启用） */
    private Integer status;

    private String remark;
}

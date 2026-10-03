package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单信息出参
 */
@Data
public class MenuVO {

    /**
     * 菜单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记：0-未删除 1-已删除
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 父菜单ID，顶级菜单为0
     */
    @JsonSerialize(using = ToStringSerializer.class)
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

    /**
     * 子菜单列表
     */
    private List<MenuVO> children;
}

package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 菜单实体（表菜单）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 菜单名称，同时作为侧边栏的显示文案（分组标题 / 菜单项文字）
     */
    private String menuName;

    /**
     * 父菜单 ID，顶级菜单为 0
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;

    /**
     * 同级排序号，越小越靠前；一级目录用 10/20/… 步长便于中间插入
     */
    private Integer sortOrder;

    /**
     * 菜单类型：1-目录（侧边栏分组） 2-菜单（真实页面） 3-按钮（仅权限标识）
     */
    private Integer menuType;

    /**
     * 前端路由地址，与 vue-router 注册的 path 一致（如 /system/user）</br>
     * 一级目录存分组前缀（如 /system），不指向具体页面
     */
    private String path;

    /**
     * 视图组件路径，相对 src/views 且不含 .vue 后缀（如 system/user/UserView）</br>
     * 目录与按钮类型为空；供「菜单驱动动态路由」使用
     */
    private String component;

    /**
     * 菜单唯一标识（点分命名，如 system.user），也是 uk_menu_key 唯一索引的列
     */
    private String menuKey;

    /**
     * 图标名，取 Element Plus 图标组件名（如 UserFilled / Key / Menu）
     */
    private String icon;

    /**
     * 权限标识（如 system:user:list）</br>
     * 经角色菜单关联汇总成登录返回的 permissions，供细粒度鉴权使用
     */
    private String permission;

    /**
     * 是否外链：0-否 1-是
     */
    private Integer isFrame;

    /**
     * 是否缓存页面：0-否 1-是
     */
    private Integer isCache;

    /**
     * 是否在侧边栏显示：0-隐藏 1-显示
     */
    private Integer isVisible;

    /**
     * 状态：0-停用 1-启用
     */
    private Integer status;

    /**
     * 子菜单（非数据库字段，构建树时装配）
     */
    @TableField(exist = false)
    private List<SysMenu> children;
}

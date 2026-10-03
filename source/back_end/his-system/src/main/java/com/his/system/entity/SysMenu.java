package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 菜单实体（表菜单）
 *
 * <p>菜单是「侧边栏 / 路由 / 权限标识」三者的公共来源：</p>
 * <ul>
 *   <li>一级目录（{@code menuType=1}）对应侧边栏的一个可折叠分组，本身不指向页面；</li>
 *   <li>二级菜单（{@code menuType=2}）对应一个真实页面，路径是前端路由地址；</li>
 *   <li>按钮（{@code menuType=3}）只承载 {@code permission} 权限标识，不出现在菜单树里。</li>
 * </ul>
 *
 * <p><b>注意</b>：{@code uk_menu_key} 是唯一索引且<b>不包含 del_flag</b>，
 * 所以删除菜单必须物理删除，置 {@code del_flag=1} 会让 menu_key 一直被占用。</p>
 *
 * <p>数据口径与初始化脚本见 sql/52-菜单表重建.sql。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

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

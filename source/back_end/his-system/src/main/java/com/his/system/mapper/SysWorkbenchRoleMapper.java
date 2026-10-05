package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.SysWorkbenchRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色工作台卡片配置 Mapper。
 *
 * <p>这里的删除一律用<b>物理</b>删除，不走删除标记软删：表上有
 * {@code UNIQUE KEY uk_workbench_role_widget (role_id, widget_id)}，唯一键不含 del_flag，
 * 软删后「整表替换」的第二步 INSERT 必然撞唯一键，配置保存变成必现 500
 * （验证脚本 A23 第一次跑就踩到了）。这两张表是纯配置数据，没有留档价值。
 */
@Mapper
public interface SysWorkbenchRoleMapper extends BaseMapper<SysWorkbenchRole> {

    /**
     * 清空某角色的全部卡片配置（保存=整体替换的第一步）
     */
    @Delete("DELETE FROM sys_workbench_role WHERE role_id = #{roleId}")
    int purgeByRole(@Param("roleId") Long roleId);

    /**
     * 卡片从注册表删除时，连带清掉所有角色对它的引用，避免留下指向已删卡片的孤儿行
     */
    @Delete("DELETE FROM sys_workbench_role WHERE widget_id = #{widgetId}")
    int purgeByWidget(@Param("widgetId") Long widgetId);
}

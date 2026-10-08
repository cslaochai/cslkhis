package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.SysWorkbenchRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色工作台卡片配置 Mapper。
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

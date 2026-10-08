package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.SysWorkbenchWidget;
import com.his.system.vo.WorkbenchWidgetVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 工作台卡片注册表 Mapper。
 */
@Mapper
public interface SysWorkbenchWidgetMapper extends BaseMapper<SysWorkbenchWidget> {

    /**
     * 某角色当前实际应渲染的卡片（已过滤：配置勾选 + 已上线 + 权限码命中）
     */
    @Select("""
            SELECT w.id,
                   w.widget_code AS widgetCode,
                   w.widget_name AS widgetName,
                   w.area,
                   w.api_key     AS apiKey,
                   w.permission,
                   w.default_span AS span,
                   x.sort_order  AS sortOrder,
                   w.status,
                   x.visible
              FROM sys_workbench_role x
              JOIN sys_workbench_widget w ON w.id = x.widget_id AND w.del_flag = 0
              JOIN sys_role r ON r.id = x.role_id AND r.del_flag = 0
             WHERE x.del_flag = 0
               AND x.visible = 1
               AND w.status = 1
               AND r.role_code = #{roleCode}
               AND (w.permission IS NULL OR EXISTS (
                       SELECT 1 FROM sys_menu m
                       JOIN sys_role_menu rm ON rm.menu_id = m.id
                        WHERE rm.role_id = r.id AND m.del_flag = 0 AND m.status = 1
                          AND m.permission = w.permission))
             ORDER BY x.sort_order, w.id
            """)
    List<WorkbenchWidgetVO> selectRoleWidgets(@Param("roleCode") String roleCode);

    /**
     * 角色一条配置都没有时的回落：只给通用三张（待办/通知/快捷入口），它们不依赖角色专属数据。
     * 保证「新角色漏配 → 首页空白」不会发生。
     */
    @Select("""
            SELECT w.id,
                   w.widget_code AS widgetCode,
                   w.widget_name AS widgetName,
                   w.area,
                   w.api_key     AS apiKey,
                   w.permission,
                   w.default_span AS span,
                   w.sort_order  AS sortOrder,
                   w.status,
                   1             AS visible
              FROM sys_workbench_widget w
             WHERE w.del_flag = 0
               AND w.status = 1
               AND w.area IN ('todo', 'notice', 'entry')
               AND (w.permission IS NULL OR EXISTS (
                       SELECT 1 FROM sys_menu m
                       JOIN sys_role_menu rm ON rm.menu_id = m.id
                       JOIN sys_role r ON r.id = rm.role_id AND r.del_flag = 0
                        WHERE r.role_code = #{roleCode}
                          AND m.del_flag = 0 AND m.status = 1
                          AND m.permission = w.permission))
             ORDER BY w.sort_order, w.id
            """)
    List<WorkbenchWidgetVO> selectFallbackWidgets(@Param("roleCode") String roleCode);

    /**
     * 配置页回显：注册表全量左连该角色配置行（未勾选也返回，visible=0）
     */
    @Select("""
            SELECT w.id,
                   w.widget_code AS widgetCode,
                   w.widget_name AS widgetName,
                   w.area,
                   w.api_key     AS apiKey,
                   w.permission,
                   w.default_span AS span,
                   IFNULL(x.sort_order, w.sort_order) AS sortOrder,
                   w.status,
                   IFNULL(x.visible, 0) AS visible
              FROM sys_workbench_widget w
              LEFT JOIN sys_workbench_role x ON x.widget_id = w.id AND x.role_id = #{roleId} AND x.del_flag = 0
             WHERE w.del_flag = 0
             ORDER BY FIELD(w.area, 'todo', 'notice', 'entry', 'kpi', 'domain'),
                      IFNULL(x.sort_order, w.sort_order), w.id
            """)
    List<WorkbenchWidgetVO> selectAllWidgetsForRole(@Param("roleId") Long roleId);

    /**
     * 物理删除卡片。
     *
     * <p>不走删除标记软删：表上有 {@code UNIQUE KEY uk_workbench_widget_code (widget_code)}
     * 且不含 del_flag，软删后同一个编码（往往就是打错字重来那一回）再也插不进去，
     * 配置页会变成一个无法解释的 500。注册表是纯配置数据，留档没有意义。
     */
    @Delete("DELETE FROM sys_workbench_widget WHERE id = #{widgetId}")
    int purgeById(@Param("widgetId") Long widgetId);

    /**
     * 落点策略：同一角色多行时取最大值兜底（铺底按角色整体 UPDATE，正常只会有一个值）
     */
    @Select("""
            SELECT MAX(x.landing_scope)
              FROM sys_workbench_role x
              JOIN sys_role r ON r.id = x.role_id AND r.del_flag = 0
             WHERE x.del_flag = 0 AND r.role_code = #{roleCode}
            """)
    Integer selectLandingScope(@Param("roleCode") String roleCode);
}

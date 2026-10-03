package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 通过员工ID查询角色（院内用户）
     * <p>
     * 必须带 ORDER BY：返回顺序即「该账号的第一个角色」（登录时不指定角色时取它，
     * 见 LoginController#determineCurrentRole），原实现用 SELECT DISTINCT 无排序，
     * 顺序取决于执行计划（不可依赖）。排序口径 = 角色的排序医嘱，
     * 运维改 sort_order 即可调整默认角色。
     * 注意：DISTINCT + ORDER BY 非选择列在 MySQL 8 会报错，故用 GROUP BY + 聚合排序。
     */
    @Select("SELECT r.role_code FROM sys_role r " +
            "INNER JOIN sys_employee_post ur ON r.id = ur.role_id " +
            "WHERE ur.employee_id = #{employeeId} AND r.del_flag = 0 " +
            "GROUP BY r.role_code " +
            "ORDER BY MIN(r.sort_order), MIN(r.id)")
    List<String> selectRolesByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * 通过员工ID查询权限（院内用户）
     * <p>
     * ⚠ 这是<b>员工级、全部角色的并集</b>，不是"当前角色"的权限。
     * 方法级鉴权不能直接用这个集合（多角色账号切角色后越权），
     * 过滤器会再按 token 里的 currentRole 调 {@link #selectPermissionsByRoleCode} 覆盖一遍。
     */
    @Select("SELECT DISTINCT m.permission FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_employee_post ur ON rm.role_id = ur.role_id " +
            "WHERE ur.employee_id = #{employeeId} AND m.del_flag = 0 AND m.permission IS NOT NULL AND m.permission != ''")
    List<String> selectPermissionsByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * 通过角色编码查询权限（= 该角色被授的菜单里配了权限码的那些）。
     *
     * <p>口径与 {@link #selectPermissionsByEmployeeId} 一致，只是把"员工 → 角色"这层固定成单个角色。
     * 角色配菜单即配权限，不另建映射表。
     */
    @Select("SELECT DISTINCT m.permission FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_role r ON r.id = rm.role_id " +
            "WHERE r.role_code = #{roleCode} AND r.del_flag = 0 AND m.del_flag = 0 " +
            "AND m.permission IS NOT NULL AND m.permission != ''")
    List<String> selectPermissionsByRoleCode(@Param("roleCode") String roleCode);
}

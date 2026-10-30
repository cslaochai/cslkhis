package com.his.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.platform.entity.SysUser;
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
     */
    @Select("SELECT r.role_code FROM sys_role r " +
            "INNER JOIN sys_employee_post ur ON r.id = ur.role_id " +
            "WHERE ur.employee_id = #{employeeId} AND r.del_flag = 0 " +
            "GROUP BY r.role_code " +
            "ORDER BY MIN(r.sort_order), MIN(r.id)")
    List<String> selectRolesByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * 通过员工ID查询权限（院内用户）
     */
    @Select("SELECT DISTINCT m.permission FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_employee_post ur ON rm.role_id = ur.role_id " +
            "WHERE ur.employee_id = #{employeeId} AND m.del_flag = 0 AND m.permission IS NOT NULL AND m.permission != ''")
    List<String> selectPermissionsByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * 通过角色编码查询权限（= 该角色被授的菜单里配了权限码的那些）。
     */
    @Select("SELECT DISTINCT m.permission FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_role r ON r.id = rm.role_id " +
            "WHERE r.role_code = #{roleCode} AND r.del_flag = 0 AND m.del_flag = 0 " +
            "AND m.permission IS NOT NULL AND m.permission != ''")
    List<String> selectPermissionsByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 通过角色ID查询持有该角色的所有院内账号用户名（去重）。
     * 链路：sys_user.emp_id → sys_employee.id → sys_employee_post.employee_id，按 role_id 过滤。
     * 用于角色权限变更后精准失效其名下账号的身份缓存（而非全量 invalidateAll）。
     */
    @Select("SELECT DISTINCT u.user_name FROM sys_user u " +
            "INNER JOIN sys_employee e ON u.emp_id = e.id " +
            "INNER JOIN sys_employee_post p ON e.id = p.employee_id " +
            "WHERE p.role_id = #{roleId}")
    List<String> selectUserNamesByRoleId(@Param("roleId") Long roleId);

    /**
     * 持有指定权限码的全部员工ID（去重）。
     * 链路：sys_menu.permission → sys_role_menu → sys_employee_post.employee_id。
     * 用于「谁有处置权就通知谁」的预警接收人圈定（如无签结诊告警发给医务科）。
     */
    @Select("SELECT DISTINCT p.employee_id FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_employee_post p ON rm.role_id = p.role_id " +
            "WHERE m.permission = #{permission} AND m.del_flag = 0")
    List<Long> selectEmployeeIdsByPermission(@Param("permission") String permission);
}

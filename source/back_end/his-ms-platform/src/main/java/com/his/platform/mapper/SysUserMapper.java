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
     * 持有指定权限码的全部员工ID（去重）。
     * 链路：sys_menu.permission → sys_role_menu → sys_employee_post.employee_id。
     * 用于「谁有处置权就通知谁」的预警接收人圈定（如无签结诊告警发给医务科）。
     */
    @Select("SELECT DISTINCT p.employee_id FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_employee_post p ON rm.role_id = p.role_id " +
            "WHERE m.permission = #{permission} AND m.del_flag = 0")
    List<Long> selectEmployeeIdsByPermission(@Param("permission") String permission);

    /**
     * 持有指定权限码、且名下没有指定角色编码岗位的全部员工ID（去重）。
     *
     * <p>与上一条的区别只在 NOT EXISTS：超管为应急处置持业务权限码（sql/412），
     * 按权限码圈业务催办接收人时要按角色编码把它排除，否则业务提醒全落超管。
     */
    @Select("SELECT DISTINCT p.employee_id FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_employee_post p ON rm.role_id = p.role_id " +
            "WHERE m.permission = #{permission} AND m.del_flag = 0 " +
            "AND NOT EXISTS (SELECT 1 FROM sys_employee_post ep " +
            "INNER JOIN sys_role er ON er.id = ep.role_id " +
            "WHERE ep.employee_id = p.employee_id AND er.role_code = #{excludeRoleCode} AND er.del_flag = 0)")
    List<Long> selectEmployeeIdsByPermissionExcludingRole(@Param("permission") String permission,
                                                          @Param("excludeRoleCode") String excludeRoleCode);
}

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
}

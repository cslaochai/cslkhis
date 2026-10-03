package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单Mapper
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_employee_post ur ON rm.role_id = ur.role_id " +
            "WHERE ur.employee_id = #{employeeId} AND m.del_flag = 0 AND m.status = 1 " +
            // 兜底键必须带 id：sort_order 允许并列，只按它排时并列行的顺序取决于执行计划
            "ORDER BY m.sort_order, m.id")
    List<SysMenu> selectMenusByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * 按「员工 + 当前角色」查询菜单
     *
     * 权限口径：菜单展示跟随**当前角色**（JWT 里带 currentRole，切换角色会换 token），
     * 而不是员工全部角色的并集。并集会让多角色员工（如同时挂医生 + 药剂师）越权看到
     * 另一个角色的菜单，「切换角色」也就失去意义。
     *
     * 返回结果里可能同时含菜单与它的一级目录，交由调用方 buildMenuTree 组装。
     */
    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_employee_post ur ON rm.role_id = ur.role_id " +
            "INNER JOIN sys_role r ON r.id = ur.role_id " +
            "WHERE ur.employee_id = #{employeeId} AND r.role_code = #{roleCode} " +
            "AND r.del_flag = 0 AND r.status = 1 " +
            "AND m.del_flag = 0 AND m.status = 1 " +
            "ORDER BY m.sort_order, m.id")
    List<SysMenu> selectMenusByEmployeeIdAndRole(@Param("employeeId") Long employeeId,
                                                 @Param("roleCode") String roleCode);
}

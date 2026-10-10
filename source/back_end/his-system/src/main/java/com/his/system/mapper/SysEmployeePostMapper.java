package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.SysDepartList;
import com.his.system.entity.SysEmployeePost;
import com.his.system.vo.EmployeePostVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysEmployeePostMapper extends BaseMapper<SysEmployeePost> {

    /**
     * 该员工的岗位列表（角色 × 科室），顶栏「切换岗位」与管理端回显共用这一份口径。
     */
    @Select("select a.role_id, r.role_code, r.role_name, a.dept_id, b.dept_code, b.dept_name, a.is_primary, "
            + "a.effective_date, a.expire_date "
            + "from sys_employee_post a "
            + "inner join sys_role r on r.id = a.role_id and r.del_flag = 0 "
            + "inner join sys_department b on b.id = a.dept_id and b.del_flag = 0 "
            + "where a.employee_id = #{employeeId} "
            + "order by a.is_primary desc, r.sort_order, b.sort_order")
    List<EmployeePostVO> getEmployeePosts(Long employeeId);

    /**
     * 该员工在**某个角色下**被授权的科室 —— 切换岗位后的科室数据权限收口范围。
     */
    @Select("select distinct a.dept_id from sys_employee_post a "
            + "inner join sys_role r on r.id = a.role_id and r.del_flag = 0 "
            + "where a.employee_id = #{employeeId} and r.role_code = #{roleCode} "
            + "and (a.effective_date is null or a.effective_date <= curdate()) "
            + "and (a.expire_date is null or a.expire_date >= curdate())")
    List<Long> selectDeptIdsByRole(Long employeeId, String roleCode);

    /**
     * 该员工的执业科室（去重到科室维度）。
     */
    @Select("select a.dept_id, b.dept_code, b.dept_name, max(a.is_primary) as is_primary "
            + "from sys_employee_post a "
            + "inner join sys_department b on a.dept_id = b.id and b.del_flag = 0 "
            + "where a.employee_id = #{employeeId} "
            + "group by a.dept_id, b.dept_code, b.dept_name "
            + "order by b.sort_order")
    List<SysDepartList> getEmployeeDepts(Long employeeId);

}

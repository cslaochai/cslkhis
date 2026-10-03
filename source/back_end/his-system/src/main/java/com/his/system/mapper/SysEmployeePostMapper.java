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
     *
     * <p>科室走 INNER JOIN：字典里已不存在（被物理删）的科室不该出现在可切换列表里，
     * 让用户切过去只会得到一堆按空科室取数的页面。
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
     *
     * <p>按角色过滤是关键：整张表取 distinct dept_id 的话，一个「骨科医生 + 药房药剂师」
     * 的账号切成医生之后，授权集合里仍留着药房，等于岗位切了、数据范围没切。
     *
     * <p>生效期收口（sql/113）：失效日期已过或尚未生效的岗位不再进授权集合，
     * 两侧 NULL = 不限，存量数据行为中性。
     */
    @Select("select distinct a.dept_id from sys_employee_post a "
            + "inner join sys_role r on r.id = a.role_id and r.del_flag = 0 "
            + "where a.employee_id = #{employeeId} and r.role_code = #{roleCode} "
            + "and (a.effective_date is null or a.effective_date <= curdate()) "
            + "and (a.expire_date is null or a.expire_date >= curdate())")
    List<Long> selectDeptIdsByRole(Long employeeId, String roleCode);

    /**
     * 该员工的执业科室（去重到科室维度）。
     *
     * <p>岗位表一个科室可能有多条（每角色一条），故 group by 科室而不是 distinct 整行：
     * 同一科室在角色A下是主岗位、角色B下不是，distinct 会把它算成两行。
     */
    @Select("select a.dept_id, b.dept_code, b.dept_name, max(a.is_primary) as is_primary "
            + "from sys_employee_post a "
            + "inner join sys_department b on a.dept_id = b.id and b.del_flag = 0 "
            + "where a.employee_id = #{employeeId} "
            + "group by a.dept_id, b.dept_code, b.dept_name "
            + "order by b.sort_order")
    List<SysDepartList> getEmployeeDepts(Long employeeId);

}

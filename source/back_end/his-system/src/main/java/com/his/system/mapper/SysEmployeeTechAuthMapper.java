package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.SysEmployeeTechAuth;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysEmployeeTechAuthMapper extends BaseMapper<SysEmployeeTechAuth> {

    /**
     * 物理删：本表无 del_flag，deleteById 已是物理删；此方法留给「按人清场」的测试夹具。
     * 撞的是 uk_emp_cat_from（employee_id + auth_category + valid_from），软删会占键。
     */
    @Delete("DELETE FROM sys_employee_tech_auth WHERE employee_id = #{employeeId}")
    int purgeByEmployee(@Param("employeeId") Long employeeId);
}

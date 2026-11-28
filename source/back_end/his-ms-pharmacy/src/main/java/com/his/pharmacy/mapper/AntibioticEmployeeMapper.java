package com.his.pharmacy.mapper;

import com.his.pharmacy.vo.AntibioticDoctorSelectListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 医师主数据只读（裸 SQL 读员工；仅保留单值快照，候选清单走员工宿主 selectList）。
 */
@Mapper
public interface AntibioticEmployeeMapper {

    @Select("""
            SELECT e.id, e.emp_name AS doctorName, e.dept_id AS deptId, e.dept_name AS deptName, e.title
            FROM sys_employee e
            WHERE e.del_flag = 0 AND e.id = #{id}
            """)
    AntibioticDoctorSelectListVO selectDoctorById(@Param("id") Long id);
}

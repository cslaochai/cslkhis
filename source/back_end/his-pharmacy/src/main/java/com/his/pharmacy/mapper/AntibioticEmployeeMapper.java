package com.his.pharmacy.mapper;

import com.his.pharmacy.vo.AntibioticDoctorSelectListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 医师主数据只读（裸 SQL 读员工）。
 *
 * <p>his-emr 虽然 Maven 上依赖了 his-system，但按 AGENTS 铁律「跨模块读异模块表用裸 SQL Mapper」，
 * 这里不引入 SysEmployee 实体 —— 授权表只需要"这个医师叫什么、哪个科、什么职称"这三个快照字段。
 */
@Mapper
public interface AntibioticEmployeeMapper {

    /** 医师下拉（限医生岗 emp_type=1，最多 50 条；超了请输关键字） */
    @Select("""
            <script>
            SELECT e.id, e.emp_name AS doctorName, e.dept_id AS deptId, e.dept_name AS deptName, e.title
            FROM sys_employee e
            WHERE e.del_flag = 0 AND e.emp_type = 1 AND e.status = 1
              <if test="keyword != null and keyword != ''">
                AND (e.emp_name LIKE CONCAT('%', #{keyword}, '%') OR e.dept_name LIKE CONCAT('%', #{keyword}, '%'))
              </if>
            ORDER BY e.id
            LIMIT 50
            </script>
            """)
    List<AntibioticDoctorSelectListVO> selectDoctors(@Param("keyword") String keyword);

    @Select("""
            SELECT e.id, e.emp_name AS doctorName, e.dept_id AS deptId, e.dept_name AS deptName, e.title
            FROM sys_employee e
            WHERE e.del_flag = 0 AND e.id = #{id}
            """)
    AntibioticDoctorSelectListVO selectDoctorById(@Param("id") Long id);
}

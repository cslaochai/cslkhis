package com.his.platform.mapper;

import com.his.platform.vo.SimClinicRosterVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 仿真实验室门诊花名册只读查询。
 */
@Mapper
public interface SimRosterMapper {

    /**
     * 门诊科室下持 10013（医生）岗位且有登录账号的员工，连带本科室诊室。
     * 行按科室、员工、诊室排序，服务层每科取首行作为一个出诊单元。
     */
    @Select("""
            SELECT p.dept_id AS deptId, d.dept_name AS deptName,
                   p.employee_id AS doctorId, e.emp_name AS doctorName, u.user_name AS username,
                   r.id AS roomId, r.name AS roomName
            FROM sys_employee_post p
            JOIN sys_user u ON u.emp_id = p.employee_id AND u.status = 1 AND u.del_flag = 0
            JOIN sys_employee e ON e.id = p.employee_id AND e.status = 1 AND e.del_flag = 0
            JOIN sys_role ro ON ro.id = p.role_id AND ro.role_code = '10013'
            JOIN sys_department d ON d.id = p.dept_id AND d.node_type = '2' AND d.status = 1
                AND d.del_flag = 0 AND FIND_IN_SET('1', d.dept_type)
            LEFT JOIN sys_clinic_room r ON r.dept_id = p.dept_id AND r.status = 1 AND r.del_flag = 0
            WHERE e.dept_id = p.dept_id
            ORDER BY p.dept_id, p.employee_id, r.id
            """)
    List<SimClinicRosterVO> listDoctorClinics();

    /**
     * 各科室持 10020（门诊分诊护士）岗位的登录账号，每科取首条。
     */
    @Select("""
            SELECT p.dept_id AS deptId, u.user_name AS triageUsername
            FROM sys_employee_post p
            JOIN sys_user u ON u.emp_id = p.employee_id AND u.status = 1 AND u.del_flag = 0
            JOIN sys_role ro ON ro.id = p.role_id AND ro.role_code = '10020'
            ORDER BY p.dept_id, p.employee_id
            """)
    List<SimClinicRosterVO> listTriage();
}

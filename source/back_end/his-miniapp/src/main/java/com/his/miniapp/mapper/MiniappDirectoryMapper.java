package com.his.miniapp.mapper;

import com.his.miniapp.vo.DeptSelectListVO;
import com.his.miniapp.vo.DoctorSelectListVO;
import com.his.miniapp.vo.PatientDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 挂号目录跨模块只读 Mapper（裸 SQL，铁律：跨模块读异模块表用裸 SQL Mapper）。
 *
 * <p>患者端挂号页的科室/医生下拉；号源走 his-appoint 的
 * {@code ScheduleService.scheduleSelectList}（服务层可跨模块调用）。
 */
@Mapper
public interface MiniappDirectoryMapper {

    /** 开放中的科室（挂号可选；主键 CAST 防精度丢失） */
    @Select("""
            SELECT CAST(id AS CHAR) AS id, dept_name, dept_type, dept_desc
            FROM sys_department
            WHERE del_flag = 0 AND is_open = 1 AND status = 1
            ORDER BY sort_order ASC, id ASC
            """)
    List<DeptSelectListVO> selectOpenDepartments();

    /** 某科室的医生名册（挂号选医生，含职称/擅长/专家费展示；主键 CAST 防精度丢失） */
    @Select("""
            SELECT CAST(id AS CHAR) AS id, emp_name, dept_id, dept_name, title, specialty,
                   is_expert, expert_price
            FROM sys_employee
            WHERE del_flag = 0 AND status = 1 AND dept_id = #{deptId}
            ORDER BY id ASC
            """)
    List<DoctorSelectListVO> selectDoctorsByDept(@Param("deptId") Long deptId);

    /** 患者主档（档案页展示；主键 CAST 成字符串防 JS 端 BIGINT 精度丢失） */
    @Select("""
            SELECT CAST(id AS CHAR) AS id, patient_no, patient_name, gender, birth_date,
                   age, phone, id_card, balance, visit_count
            FROM biz_patient
            WHERE id = #{patientId} AND del_flag = 0
            LIMIT 1
            """)
    PatientDetailVO selectPatientById(@Param("patientId") Long patientId);
}

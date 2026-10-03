package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.emr.entity.BizInfectionCase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface BizInfectionCaseMapper extends BaseMapper<BizInfectionCase> {

    /**
     * 按编号查（含软删行）——病例编号有唯一键，查重必须含软删行。
     */
    @Select("SELECT COUNT(*) FROM biz_infection_case WHERE case_no = #{caseNo}")
    int countByNoIncludingDeleted(@Param("caseNo") String caseNo);

    /**
     * 患者快照（编号/姓名/性别/年龄）：病例是质控凭证，档案改名不影响历史数据。
     */
    @Select("SELECT patient_no AS patientNo, patient_name AS patientName, gender, age,"
            + " id AS patientId FROM biz_patient WHERE id = #{patientId} AND del_flag = 0")
    Map<String, Object> selectPatientSnapshot(@Param("patientId") Long patientId);

    /**
     * 门诊挂号单科室（发现科室兜底来源）。
     */
    @Select("SELECT dept_id AS deptId, dept_name AS deptName FROM biz_appoint_info"
            + " WHERE id = #{registId} AND del_flag = 0")
    Map<String, Object> selectRegistDept(@Param("registId") Long registId);

    /**
     * 科室名快照（手卫生观察登记用）。
     */
    @Select("SELECT id AS deptId, dept_name AS deptName FROM sys_department"
            + " WHERE id = #{deptId} AND del_flag = 0")
    Map<String, Object> selectDeptName(@Param("deptId") Long deptId);

    /**
     * 住院登记科室（发现科室兜底来源；入院记录的科室ID = 当前科室，主键 admission_id）。
     */
    @Select("SELECT dept_id AS deptId, (SELECT dept_name FROM sys_department d WHERE d.id = a.dept_id) AS deptName"
            + " FROM biz_admission a WHERE a.admission_id = #{inpId} AND a.del_flag = 0")
    Map<String, Object> selectInpDept(@Param("inpId") Long inpId);
}

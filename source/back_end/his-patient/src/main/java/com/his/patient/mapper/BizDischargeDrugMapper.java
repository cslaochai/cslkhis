package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.entity.BizDischargeDrug;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 出院带药单 Mapper。
 */
@Mapper
public interface BizDischargeDrugMapper extends BaseMapper<BizDischargeDrug> {

    /**
     * 入院次归属的患者ID（his-patient 本域入院记录，直接裸查列名已对 information_schema 核对）
     */
    @Select("SELECT patient_id FROM biz_admission WHERE admission_id = #{admissionId} AND del_flag = 0")
    Long selectPatientIdByAdmission(Long admissionId);

    /**
     * 入院次归属的科室ID（数据权限折算用；取不到返回 NULL）
     */
    @Select("SELECT dept_id FROM biz_admission WHERE admission_id = #{admissionId} AND del_flag = 0")
    Long selectAdmissionDeptId(Long admissionId);

    /**
     * 分页（科室数据权限：带药单挂在住院上，经 biz_admission.dept_id 收口）
     */
    @Select("""
            <script>
            SELECT d.* FROM biz_discharge_drug d
                     LEFT JOIN biz_admission a ON a.admission_id = d.admission_id AND a.del_flag = 0
            WHERE d.del_flag = 0
              <if test="admissionId != null"> AND d.admission_id = #{admissionId}</if>
              <if test="patientId != null"> AND d.patient_id = #{patientId}</if>
              <if test="drugName != null and drugName != ''"> AND d.drug_name LIKE CONCAT('%', #{drugName}, '%')</if>
              <if test="dispenseStatus != null"> AND d.dispense_status = #{dispenseStatus}</if>
              <if test="deptIds != null"> AND a.dept_id IN <foreach collection="deptIds" item="dd" open="(" separator="," close=")">#{dd}</foreach></if>
            ORDER BY d.id DESC
            </script>
            """)
    IPage<BizDischargeDrug> selectScopedPage(IPage<BizDischargeDrug> page,
                                             @Param("admissionId") Long admissionId,
                                             @Param("patientId") Long patientId,
                                             @Param("drugName") String drugName,
                                             @Param("dispenseStatus") Integer dispenseStatus,
                                             @Param("deptIds") List<Long> deptIds);

    /**
     * 患者快照（患者基本信息列：patient_no / patient_name，跨表裸 SQL 已对列名核对）
     */
    @Select("SELECT id, patient_no AS patientNo, patient_name AS patientName FROM biz_patient "
            + "WHERE id = (SELECT patient_id FROM biz_admission WHERE admission_id = #{admissionId} AND del_flag = 0) "
            + "AND del_flag = 0 LIMIT 1")
    BizDischargeDrug selectPatientSnapshot(Long admissionId);
}

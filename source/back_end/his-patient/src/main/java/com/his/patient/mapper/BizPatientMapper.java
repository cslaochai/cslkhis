package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizPatient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 患者Mapper
 */
@Mapper
public interface BizPatientMapper extends BaseMapper<BizPatient> {

    /**
     * 就诊结诊 → 回写患者主档「最近就诊」冗余组（last_visit_time + 科室/医生 ID与名）。
     *
     * @return 实际更新的行数（0 = 该次结诊不改变最近就诊）
     */
    @Update("UPDATE biz_patient SET last_visit_time = #{visitTime}, " +
            "last_visit_dept = #{deptId}, last_visit_dept_name = #{deptName}, " +
            "last_visit_doctor = #{doctorId}, last_visit_doctor_name = #{doctorName} " +
            "WHERE id = #{patientId} AND del_flag = 0 " +
            "AND (last_visit_time IS NULL OR #{visitTime} >= last_visit_time)")
    int markLastVisit(@Param("patientId") Long patientId, @Param("visitTime") LocalDateTime visitTime,
                      @Param("deptId") Long deptId, @Param("deptName") String deptName,
                      @Param("doctorId") Long doctorId, @Param("doctorName") String doctorName);

    /**
     * 就诊结诊 → 回写患者主档「首次就诊」冗余组（first_visit_time + 科室/医生 ID与名）。
     *
     * @return 实际更新的行数（0 = 已有更早的首次就诊，不覆盖）
     */
    @Update("UPDATE biz_patient SET first_visit_time = #{visitTime}, " +
            "first_visit_dept_id = #{deptId}, first_visit_dept_name = #{deptName}, " +
            "first_visit_doctor_id = #{doctorId}, first_visit_doctor_name = #{doctorName} " +
            "WHERE id = #{patientId} AND del_flag = 0 " +
            "AND (first_visit_time IS NULL OR #{visitTime} < first_visit_time)")
    int markFirstVisit(@Param("patientId") Long patientId, @Param("visitTime") LocalDateTime visitTime,
                       @Param("deptId") Long deptId, @Param("deptName") String deptName,
                       @Param("doctorId") Long doctorId, @Param("doctorName") String doctorName);

    /**
     * 批量统计患者的挂号（预约）次数。
     *
     * @param ids 患者ID集合
     * @return [{patientId=xx, cnt=n}, ...]；从未挂过号的患者不会出现在结果里
     */
    @Select("<script>" +
            "SELECT patient_id AS patientId, COUNT(*) AS cnt FROM biz_appoint_info " +
            "WHERE del_flag = 0 AND patient_id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> " +
            "GROUP BY patient_id" +
            "</script>")
    List<Map<String, Object>> countRegistByPatientIds(@Param("ids") Collection<Long> ids);
}

package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.emr.entity.BizSingleDiseaseCase;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/** 单病种病例 Mapper */
@Mapper
public interface BizSingleDiseaseCaseMapper extends BaseMapper<BizSingleDiseaseCase> {

    /**
     * 病案首页取数（纳入时服务端重查快照）：出院首页（summary_status=编码完成/归档态均可，
     * 只要求有首页）+ 住院信息。无首页返回 null → 拒绝纳入。
     */
    @Select("""
            SELECT a.admission_id        AS admissionId,
                   a.patient_id          AS patientId,
                   p.patient_name        AS patientName,
                   s.main_diagnosis_code AS mainDiagnosisCode,
                   s.main_diagnosis_name AS mainDiagnosisName,
                   s.inpatient_days      AS inpatientDays,
                   s.total_amount        AS totalAmount,
                   s.is_surgery          AS isSurgery,
                   s.death_flag          AS deathFlag
              FROM biz_admission a
             INNER JOIN biz_inpatient_summary s ON s.admission_id = a.admission_id AND s.del_flag = 0
              LEFT JOIN biz_patient p ON p.id = a.patient_id AND p.del_flag = 0
             WHERE a.admission_id = #{admissionId} AND a.del_flag = 0
            """)
    Map<String, Object> selectSummarySnapshot(@Param("admissionId") Long admissionId);

    /**
     * 物理删除底账行：唯一键 {@code (disease_id, admission_id)} 不含 del_flag，
     * 自动扫描重跑前若曾软删会撞键（本表按铁律只增不改，正常不删；保留给测试清理）。
     */
    @Delete("DELETE FROM biz_single_disease_case WHERE id = #{id}")
    int purgeById(Long id);

    /**
     * 自动扫描候选：已出院 + 有首页 + 主要诊断命中病种 ICD 前缀 + 尚未纳入该病种。
     * 前缀 LIKE 转义安全：前缀本身只含字母数字与 '.'（服务端已归一大写）。
     */
    @Select("""
            <script>
            SELECT a.admission_id        AS admissionId,
                   a.patient_id          AS patientId,
                   p.patient_name        AS patientName,
                   s.main_diagnosis_code AS mainDiagnosisCode,
                   s.main_diagnosis_name AS mainDiagnosisName
              FROM biz_admission a
             INNER JOIN biz_inpatient_summary s ON s.admission_id = a.admission_id AND s.del_flag = 0
              LEFT JOIN biz_patient p ON p.id = a.patient_id AND p.del_flag = 0
             WHERE a.del_flag = 0 AND a.admit_status = 0
               AND s.main_diagnosis_code IS NOT NULL
               AND (
               <foreach collection="prefixes" item="p" separator=" OR ">
                   UPPER(s.main_diagnosis_code) LIKE CONCAT(#{p}, '%')
               </foreach>
               )
               AND NOT EXISTS (
                   SELECT 1 FROM biz_single_disease_case c
                    WHERE c.disease_id = #{diseaseId} AND c.admission_id = a.admission_id
               )
               <if test="beginDate != null and beginDate != ''">
                   AND s.discharge_time &gt;= CONCAT(#{beginDate}, ' 00:00:00')
               </if>
               <if test="endDate != null and endDate != ''">
                   AND s.discharge_time &lt;= CONCAT(#{endDate}, ' 23:59:59')
               </if>
             ORDER BY a.admission_id DESC
            </script>
            """)
    java.util.List<Map<String, Object>> selectAutoEnrollCandidates(@Param("diseaseId") Long diseaseId,
                                                                   @Param("prefixes") java.util.List<String> prefixes,
                                                                   @Param("beginDate") String beginDate,
                                                                   @Param("endDate") String endDate);
}

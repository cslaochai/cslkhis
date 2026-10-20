package com.his.drgdip.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.drgdip.entity.DipSimResult;
import com.his.drgdip.vo.DipAuxRowVO;
import com.his.drgdip.vo.DipDiseaseRowVO;
import com.his.drgdip.vo.DipSummaryListRowVO;
import com.his.drgdip.vo.DipSummaryRowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * DIP 模拟 Mapper：模拟结果走 MP，病案首页与病种目录（病种 + 辅助目录）跨模块只读走裸 SQL。
 */
@Mapper
public interface DipSimMapper extends BaseMapper<DipSimResult> {

    /**
     * 病案首页 + 实际费用（与 DRG 模拟同一输入：费用用标量子查询，避免中途结算账单 join 放大行数）。
     */
    String SUMMARY_SELECT = """
            SELECT s.id AS summaryId, s.patient_name AS patientName,
                   s.admission_id AS admissionId, s.gender AS gender, s.age AS age, s.age_unit AS ageUnit,
                   s.main_diagnosis_code AS mainDiagnosisCode, s.main_diagnosis_name AS mainDiagnosisName,
                   s.is_surgery AS isSurgery, s.inpatient_days AS inpatientDays,
                   s.birth_weight AS birthWeight,
                   IFNULL(IFNULL(s.total_amount, (
                       SELECT SUM(b.total_amount) FROM biz_settlement_bill b
                        WHERE b.del_flag = 0 AND b.encounter_type = 2 AND b.bill_type = 4
                          AND b.bill_status <> 4 AND b.encounter_id = s.admission_id)), 0) AS actualAmount
              FROM biz_inpatient_summary s
             WHERE s.del_flag = 0
            """;

    @Select(SUMMARY_SELECT + " AND s.id = #{summaryId} LIMIT 1")
    DipSummaryRowVO selectSummary(@Param("summaryId") Long summaryId);

    @Select(SUMMARY_SELECT + " ORDER BY s.id DESC LIMIT #{limit}")
    List<DipSummaryRowVO> selectSummaries(@Param("limit") int limit);

    /**
     * 可模拟首页列表（带已有模拟结果标记，结果按版本隔离）
     */
    @Select("""
            SELECT s.id AS summaryId, s.patient_name AS patientName, s.dept_name AS deptName,
                   s.discharge_time AS dischargeTime,
                   s.main_diagnosis_code AS mainDiagnosisCode, s.main_diagnosis_name AS mainDiagnosisName,
                   s.is_surgery AS isSurgery, r.dip_code AS dipCode, r.profit_amount AS profitAmount
              FROM biz_inpatient_summary s
              LEFT JOIN biz_dip_sim_result r ON r.summary_id = s.id AND r.version = #{version} AND r.del_flag = 0
             WHERE s.del_flag = 0
             ORDER BY s.id DESC
             LIMIT #{limit}
            """)
    List<DipSummaryListRowVO> summaryList(@Param("limit") int limit, @Param("version") String version);

    /**
     * DIP 病种目录（病种目录页与分组方案快照共用这一条查询）
     */
    @Select("""
            SELECT id, disease_code AS diseaseCode, disease_name AS diseaseName, disease_type AS diseaseType,
                   main_diag_code AS mainDiagCode, main_diag_name AS mainDiagName,
                   oper_code AS operCode, oper_name AS operName, diag_match AS diagMatch,
                   base_score AS baseScore, source, version, status
              FROM biz_dip_disease WHERE del_flag = 0 AND status = 1 AND version = #{version}
             ORDER BY disease_type, disease_code
            """)
    List<DipDiseaseRowVO> diseaseList(@Param("version") String version);

    /**
     * DIP 辅助目录（启用的，含全局规则行 disease_code 为空）
     */
    @Select("""
            SELECT disease_code AS diseaseCode, aux_type AS auxType, aux_code AS auxCode,
                   aux_name AS auxName, age_min AS ageMin, age_max AS ageMax, coeff
              FROM biz_dip_aux WHERE del_flag = 0 AND status = 1 AND version = #{version}
             ORDER BY id
            """)
    List<DipAuxRowVO> auxList(@Param("version") String version);

    /**
     * 主手术编码列表（is_main=1，首页可标多条）
     */
    @Select("""
            SELECT operation_code FROM biz_inpatient_operation
             WHERE del_flag = 0 AND is_main = 1 AND admission_id = #{admissionId}
            """)
    List<String> selectMainOperCodes(@Param("admissionId") Long admissionId);

    /**
     * 其他诊断编码列表（diag_type=2，辅助目录的合并症/肿瘤条件按此判定）
     */
    @Select("""
            SELECT icd_code FROM biz_inpatient_diagnosis
             WHERE del_flag = 0 AND diag_type = 2 AND admission_id = #{admissionId}
            """)
    List<String> selectOtherDiagCodes(@Param("admissionId") Long admissionId);
}

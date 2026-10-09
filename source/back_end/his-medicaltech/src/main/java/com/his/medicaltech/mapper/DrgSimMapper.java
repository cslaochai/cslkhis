package com.his.medicaltech.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.medicaltech.entity.DrgSimResult;
import com.his.medicaltech.vo.CcMccRowVO;
import com.his.medicaltech.vo.DrgExclusionRowVO;
import com.his.medicaltech.vo.DrgGroupRowVO;
import com.his.medicaltech.vo.DrgSummaryListRowVO;
import com.his.medicaltech.vo.DrgSummaryRowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * DRG 模拟 Mapper：DRG 分组模拟结果走 MP，病案首页/组表/CC-MCC/排除表跨模块只读走裸 SQL。
 */
@Mapper
public interface DrgSimMapper extends BaseMapper<DrgSimResult> {

    /**
     * 病案首页 + 实际费用（首页快照优先，没有再看出院结算账单；单条模拟/批量模拟共用）。
     *
     * <p>费用用标量子查询而不是 LEFT JOIN 账单表：一次住院除了出院结算还可能有中途结算账单，
     * join 会把一行首页放大成多行，DRG 模拟就凭空多出几个病例。</p>
     * <p>同时取年龄/性别/呼吸机/出生体重等分组维度（首页已存，无需二次查询）。</p>
     */
    String SUMMARY_SELECT = """
            SELECT s.id AS summaryId, s.patient_name AS patientName,
                   s.admission_id AS admissionId, s.gender AS gender, s.age AS age, s.age_unit AS ageUnit,
                   s.main_diagnosis_code AS mainDiagnosisCode, s.main_diagnosis_name AS mainDiagnosisName,
                   s.is_surgery AS isSurgery, s.inpatient_days AS inpatientDays, s.death_flag AS deathFlag,
                   s.ventilator_hours AS ventilatorHours, s.birth_weight AS birthWeight,
                   IFNULL(IFNULL(s.total_amount, (
                       SELECT SUM(b.total_amount) FROM biz_settlement_bill b
                        WHERE b.del_flag = 0 AND b.encounter_type = 2 AND b.bill_type = 4
                          AND b.bill_status <> 4 AND b.encounter_id = s.admission_id)), 0) AS actualAmount
              FROM biz_inpatient_summary s
             WHERE s.del_flag = 0
            """;

    @Select(SUMMARY_SELECT + " AND s.id = #{summaryId} LIMIT 1")
    DrgSummaryRowVO selectSummary(@Param("summaryId") Long summaryId);

    @Select(SUMMARY_SELECT + " ORDER BY s.id DESC LIMIT #{limit}")
    List<DrgSummaryRowVO> selectSummaries(@Param("limit") int limit);

    /**
     * 可模拟首页列表（带已有模拟结果标记）
     */
    @Select("""
            SELECT s.id AS summaryId, s.patient_name AS patientName, s.dept_name AS deptName,
                   s.discharge_time AS dischargeTime,
                   s.main_diagnosis_code AS mainDiagnosisCode, s.main_diagnosis_name AS mainDiagnosisName,
                   s.is_surgery AS isSurgery, r.drg_code AS drgCode, r.profit_amount AS profitAmount
              FROM biz_inpatient_summary s
              LEFT JOIN biz_drg_sim_result r ON r.summary_id = s.id AND r.del_flag = 0
             WHERE s.del_flag = 0
             ORDER BY s.id DESC
             LIMIT #{limit}
            """)
    List<DrgSummaryListRowVO> summaryList(@Param("limit") int limit);

    /**
     * 组表（CHS-DRG 方案，含扩列后的入组维度字段）
     */
    @Select("""
            SELECT id, drg_code AS drgCode, drg_name AS drgName, mdc_code AS mdcCode, adrg_code AS adrgCode,
                   weight, pay_standard AS payStandard, source, version, status,
                   group_type AS groupType, cc_mcc_flag AS ccMccFlag, gender_limit AS genderLimit,
                   age_tier AS ageTier, pre_group_flag AS preGroupFlag, surgery_attr AS surgeryAttr,
                   base_disease_flag AS baseDiseaseFlag, diag_match AS diagMatch, oper_match AS operMatch
              FROM sys_drg_group WHERE del_flag = 0 ORDER BY drg_code
            """)
    List<DrgGroupRowVO> groupList();

    /**
     * 主手术编码列表（is_main=1）
     */
    @Select("""
            SELECT operation_code FROM biz_inpatient_operation
             WHERE del_flag = 0 AND is_main = 1 AND admission_id = #{admissionId}
            """)
    List<String> selectMainOperCodes(@Param("admissionId") Long admissionId);

    /**
     * 其他诊断编码列表（diag_type=2）
     */
    @Select("""
            SELECT icd_code FROM biz_inpatient_diagnosis
             WHERE del_flag = 0 AND diag_type = 2 AND admission_id = #{admissionId}
            """)
    List<String> selectOtherDiagCodes(@Param("admissionId") Long admissionId);

    /**
     * CC/MCC 目录（官方下发，启用的）
     */
    @Select("""
            SELECT icd_code AS icdCode, cc_level AS ccLevel, version
              FROM sys_drg_ccmcc WHERE del_flag = 0 AND status = 1
            """)
    List<CcMccRowVO> selectCcMccList();

    /**
     * 排除表（主诊断下某诊断丧失 CC/MCC 资格）
     */
    @Select("""
            SELECT main_diag_code AS mainDiagCode, excluded_code AS excludedCode
              FROM sys_drg_exclusion WHERE del_flag = 0
            """)
    List<DrgExclusionRowVO> selectExclusionList();
}

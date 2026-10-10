package com.his.medicaltech.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.medicaltech.entity.DrgSimResult;
import com.his.medicaltech.vo.CcMccRowVO;
import com.his.medicaltech.vo.DrgAdrgRowVO;
import com.his.medicaltech.vo.DrgGroupRowVO;
import com.his.medicaltech.vo.DrgMdcRowVO;
import com.his.medicaltech.vo.DrgSetRowVO;
import com.his.medicaltech.vo.DrgSummaryListRowVO;
import com.his.medicaltech.vo.DrgSummaryRowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * DRG 模拟 Mapper：模拟结果走 MP，病案首页与分组方案目录（三级目录 + 码集合 + 并发症合并症）跨模块只读走裸 SQL。
 */
@Mapper
public interface DrgSimMapper extends BaseMapper<DrgSimResult> {

    /**
     * 病案首页 + 实际费用（首页快照优先，没有再看出院结算账单；单条模拟/批量模拟共用）。
     *
     * <p>费用用标量子查询而不是 LEFT JOIN 账单表：一次住院除了出院结算还可能有中途结算账单，
     * join 会把一行首页放大成多行，DRG 模拟就凭空多出几个病例。</p>
     * <p>取的维度是规则 DSL 要用的那几列（性别/年龄与单位/出生体重）；住院天数与手术编码
     * 由结果行快照或明细查询另取，官方规则不看它们。</p>
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
     * DRG 细分组目录（含规则原文与 ADRG 内排序；组表页与分组方案快照共用这一条查询）
     */
    @Select("""
            SELECT id, drg_code AS drgCode, drg_name AS drgName, mdc_code AS mdcCode, adrg_code AS adrgCode,
                   drg_rule AS drgRule, sort_no AS sortNo, weight, pay_standard AS payStandard,
                   source, version, status
              FROM sys_drg_group WHERE del_flag = 0 AND status = 1 ORDER BY sort_no, drg_code
            """)
    List<DrgGroupRowVO> groupList();

    /**
     * MDC 目录（按官方判定顺序，先期分组在最前）
     */
    @Select("""
            SELECT mdc_code AS mdcCode, mdc_name AS mdcName, mdc_rule AS mdcRule, sort_no AS sortNo
              FROM sys_drg_mdc WHERE del_flag = 0 AND status = 1 ORDER BY sort_no
            """)
    List<DrgMdcRowVO> mdcList();

    /**
     * ADRG 目录（按 MDC 内判定顺序）
     */
    @Select("""
            SELECT adrg_code AS adrgCode, adrg_name AS adrgName, adrg_rule AS adrgRule,
                   mdc_code AS mdcCode, sort_no AS sortNo
              FROM sys_drg_adrg WHERE del_flag = 0 AND status = 1 ORDER BY sort_no
            """)
    List<DrgAdrgRowVO> adrgList();

    /**
     * 规则引用的码集合（集合编号 → 精确码，含 CC/MCC 排除组）
     */
    @Select("""
            SELECT set_code AS setCode, icd_code AS icdCode
              FROM sys_drg_set WHERE del_flag = 0 AND status = 1
            """)
    List<DrgSetRowVO> setList();

    /**
     * 主手术编码列表（is_main=1，首页可标多条）
     */
    @Select("""
            SELECT operation_code FROM biz_inpatient_operation
             WHERE del_flag = 0 AND is_main = 1 AND admission_id = #{admissionId}
            """)
    List<String> selectMainOperCodes(@Param("admissionId") Long admissionId);

    /**
     * 其他手术编码列表（规则里的 QTSS，与主手术合成「手术清单」参与联合手术判定）
     */
    @Select("""
            SELECT operation_code FROM biz_inpatient_operation
             WHERE del_flag = 0 AND is_main = 0 AND admission_id = #{admissionId}
            """)
    List<String> selectOtherOperCodes(@Param("admissionId") Long admissionId);

    /**
     * 其他诊断编码列表（diag_type=2）
     */
    @Select("""
            SELECT icd_code FROM biz_inpatient_diagnosis
             WHERE del_flag = 0 AND diag_type = 2 AND admission_id = #{admissionId}
            """)
    List<String> selectOtherDiagCodes(@Param("admissionId") Long admissionId);

    /**
     * 并发症合并症目录（启用的，含每行挂的排除组编号）
     */
    @Select("""
            SELECT icd_code AS icdCode, cc_level AS ccLevel, excl_group AS exclGroup
              FROM sys_drg_ccmcc WHERE del_flag = 0 AND status = 1
            """)
    List<CcMccRowVO> ccmccList();
}

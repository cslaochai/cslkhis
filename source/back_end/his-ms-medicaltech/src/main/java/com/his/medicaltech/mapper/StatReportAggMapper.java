package com.his.medicaltech.mapper;

import com.his.medicaltech.vo.StatCohortCaseRowVO;
import com.his.medicaltech.vo.StatCohortFeesRowVO;
import com.his.medicaltech.vo.StatCohortSummaryRowVO;
import com.his.medicaltech.vo.StatInsuranceDistRowVO;
import com.his.medicaltech.vo.StatOperationLevelRowVO;
import com.his.medicaltech.vo.StatOperationRowVO;
import com.his.medicaltech.vo.StatTopDiagnosisRowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 病案统计上报聚合 Mapper（跨模块裸 SQL 一处收口，列名已按 information_schema 核对）。
 */
@Mapper
public interface StatReportAggMapper {

    String COHORT = "a.del_flag = 0 AND a.admit_status <> 7 AND a.discharge_time IS NOT NULL "
            + "AND a.discharge_time >= #{start} AND a.discharge_time <= #{end} "
            + "AND (#{deptId} = 0 OR a.dept_id = #{deptId}) ";

    /**
     * 主要诊断取数：出院记录(7) 优先，入院记录(1) 兜底，都没有则回 admission 表快照
     */
    String DX_JOIN = "LEFT JOIN biz_inpatient_record r ON r.id = ("
            + "SELECT r2.id FROM biz_inpatient_record r2 "
            + "WHERE r2.admission_id = a.admission_id AND r2.del_flag = 0 "
            + "AND r2.record_type IN (7, 1) AND IFNULL(r2.diagnosis_code, '') <> '' "
            + "ORDER BY FIELD(r2.record_type, 7, 1) LIMIT 1) ";

    String DX_CODE = "IFNULL(IFNULL(r.diagnosis_code, a.admit_diagnosis_code), '未编码')";

    String DX_NAME = "IFNULL(IFNULL(IFNULL(r.diagnosis_name, a.admit_diagnosis_name), a.diagnosis), '未填')";

    /**
     * 出院队列汇总：例数/死亡/平均住院日（脏数据 discharge&lt;admit 按行钳 0，报表不出负数）
     *
     * <p>死亡例数口径（sql/157 修正）：原先取 {@code a.admit_status = 5}，而<b>全仓没有任何代码写过
     * admit_status=5</b>（出院只置 0），所以死亡例数恒等于 0。死亡事实的唯一来源是
     * 出院记录的死亡标记 = 1（离院方式=死亡与之由出院服务成对校验）。
     */
    @Select("SELECT COUNT(*) AS dischargeCount, "
            + "IFNULL(SUM(CASE WHEN EXISTS (SELECT 1 FROM biz_discharge dd "
            + "  WHERE dd.admission_id = a.admission_id AND dd.del_flag = 0 AND dd.death_flag = 1) "
            + "  THEN 1 ELSE 0 END), 0) AS deathCount, "
            + "ROUND(IFNULL(AVG(GREATEST(TIMESTAMPDIFF(MINUTE, a.admit_time, a.discharge_time), 0) / 1440), 0), 1) AS avgLosDays "
            + "FROM biz_admission a WHERE " + COHORT)
    StatCohortSummaryRowVO cohortSummary(@Param("start") String start, @Param("end") String end,
                                      @Param("deptId") Long deptId);

    /**
     * 出院队列手术台次与三级及以上
     */
    @Select("SELECT COUNT(*) AS operationCount, "
            + "IFNULL(SUM(CASE WHEN o.operation_level >= 3 THEN 1 ELSE 0 END), 0) AS level3upCount "
            + "FROM biz_inpatient_operation o JOIN biz_admission a ON a.admission_id = o.admission_id "
            + "WHERE o.del_flag = 0 AND " + COHORT)
    StatOperationRowVO operationStats(@Param("start") String start, @Param("end") String end,
                                       @Param("deptId") Long deptId);

    /**
     * 手术级别构成（0=未录级别）
     */
    @Select("SELECT IFNULL(o.operation_level, 0) AS level, COUNT(*) AS cnt "
            + "FROM biz_inpatient_operation o JOIN biz_admission a ON a.admission_id = o.admission_id "
            + "WHERE o.del_flag = 0 AND " + COHORT
            + "GROUP BY level ORDER BY level")
    List<StatOperationLevelRowVO> operationLevelDist(@Param("start") String start, @Param("end") String end,
                                                 @Param("deptId") Long deptId);

    /**
     * 出院队列费用（L2 出院结算账单 bill_type=4，排除 4-已作废）
     */
    @Select("SELECT COUNT(*) AS settleCount, IFNULL(SUM(s.total_amount), 0) AS totalAmount, "
            + "IFNULL(SUM(s.pool_amount), 0) AS insuranceAmount, "
            + "IFNULL(SUM(s.payable_amount), 0) AS patientPayAmount, "
            + "IFNULL(SUM(GREATEST(s.payable_amount - s.paid_amount, 0)), 0) AS arrearsAmount "
            + "FROM biz_settlement_bill s JOIN biz_admission a ON a.admission_id = s.encounter_id "
            + "WHERE s.del_flag = 0 AND s.encounter_type = 2 AND s.bill_type = 4 AND s.bill_status <> 4 AND " + COHORT)
    StatCohortFeesRowVO cohortFees(@Param("start") String start, @Param("end") String end,
                                   @Param("deptId") Long deptId);

    /**
     * 险种构成（insurance_type 是 VARCHAR 存名称，非编码）
     */
    @Select("SELECT IFNULL(NULLIF(s.insurance_type, ''), '未登记') AS insuranceType, COUNT(*) AS cnt, "
            + "IFNULL(SUM(s.total_amount), 0) AS amount "
            + "FROM biz_settlement_bill s JOIN biz_admission a ON a.admission_id = s.encounter_id "
            + "WHERE s.del_flag = 0 AND s.encounter_type = 2 AND s.bill_type = 4 AND s.bill_status <> 4 AND " + COHORT
            + "GROUP BY insuranceType ORDER BY insuranceType")
    List<StatInsuranceDistRowVO> insuranceDist(@Param("start") String start, @Param("end") String end,
                                            @Param("deptId") Long deptId);

    /**
     * 主要诊断顺位前 10
     */
    @Select("SELECT " + DX_CODE + " AS diagnosisCode, " + DX_NAME + " AS diagnosisName, COUNT(*) AS cnt "
            + "FROM biz_admission a " + DX_JOIN
            + "WHERE " + COHORT
            + "GROUP BY diagnosisCode, diagnosisName ORDER BY cnt DESC, diagnosisCode LIMIT 10")
    List<StatTopDiagnosisRowVO> topDiagnoses(@Param("start") String start, @Param("end") String end,
                                           @Param("deptId") Long deptId);

    /**
     * 病例明细行（报文明细段，500 条封顶防报文失控）
     */
    @Select("SELECT a.admission_no AS admissionNo, "
            + "IFNULL(p.patient_no, '') AS patientNo, IFNULL(p.patient_name, '') AS patientName, "
            + "IFNULL(d.dept_name, '') AS deptName, "
            + "a.admit_time AS admitTime, "
            + "a.discharge_time AS dischargeTime, "
            + "ROUND(GREATEST(TIMESTAMPDIFF(MINUTE, a.admit_time, a.discharge_time), 0) / 1440, 1) AS losDays, "
            + DX_CODE + " AS diagnosisCode, " + DX_NAME + " AS diagnosisName, "
            + "IFNULL((SELECT CONCAT(o2.operation_name, '(', IFNULL(o2.operation_level, 0), '级)') "
            + "FROM biz_inpatient_operation o2 WHERE o2.admission_id = a.admission_id AND o2.del_flag = 0 "
            + "ORDER BY (o2.is_main = 1) DESC, o2.seq_no LIMIT 1), '-') AS mainOperation, "
            + "IFNULL((SELECT SUM(s2.total_amount) FROM biz_settlement_bill s2 "
            + "WHERE s2.encounter_id = a.admission_id AND s2.encounter_type = 2 AND s2.bill_type = 4 "
            + "AND s2.del_flag = 0 AND s2.bill_status <> 4), 0) AS settleAmount "
            + "FROM biz_admission a "
            + "LEFT JOIN biz_patient p ON p.id = a.patient_id AND p.del_flag = 0 "
            + "LEFT JOIN sys_department d ON d.id = a.dept_id AND d.del_flag = 0 "
            + DX_JOIN
            + "WHERE " + COHORT
            + "ORDER BY a.discharge_time LIMIT 500")
    List<StatCohortCaseRowVO> cohortCases(@Param("start") String start, @Param("end") String end,
                                          @Param("deptId") Long deptId);

    /**
     * 科室名快照（全院口径不用）
     */
    @Select("SELECT dept_name FROM sys_department WHERE id = #{deptId} AND del_flag = 0 LIMIT 1")
    String selectDeptName(@Param("deptId") Long deptId);
}

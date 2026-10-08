package com.his.pharmacy.mapper;

import com.his.pharmacy.vo.IncisionCandidateVO;
import com.his.pharmacy.vo.IncisionDrugCandidateVO;
import com.his.pharmacy.vo.MonitorDeptRowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 抗菌药物监测的复算查询（全部裸 SQL）。
 *
 * <p><b>为什么全是裸 SQL 而不是实体关联</b>：
 * 统计要跨药品字典（his-system）、入院记录 / 住院医嘱主表（his-patient）、
 * 处方主表（his-emr）、检验申请单（his-emr）四张不同模块的表。
 * 跨模块读异模块表一律裸 SQL 快照，不建外键、不让模块反向依赖（AGENTS 铁律）。
 *
 * <p><b>药品解析三级匹配（一处定义，全文复用）</b>：
 * drug_code = 医嘱 item_code → 抗菌药物品名别名.alias_name = 医嘱 item_name →
 * drug_name/generic_name = 医嘱 item_name。三级都命中不了就不计入 —— 宁可漏算，不可错算；
 * 漏掉的条数进 unmatched_order_count，提示去维护目录或别名。
 */
@Mapper
public interface AntibioticStatMapper {

    /**
     * 门急诊处方总数（口径：prescription_source IN (1,2) 且 prescription_status IN (3,4)，
     * 与处方点评 sql/160 同一口径 —— 草稿/取消/退药没有真实用药）。
     */
    @Select("""
            SELECT COUNT(*) FROM biz_prescription p
            WHERE p.del_flag = 0
              AND p.prescription_source IN (1, 2)
              AND p.prescription_status IN (3, 4)
              AND p.visit_date BETWEEN #{from} AND #{to}
              AND (#{deptId} IS NULL OR p.dept_id = #{deptId})
            """)
    long countOpRx(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("deptId") Long deptId);

    /**
     * 含抗菌药物的门急诊处方数（处方明细里有 antibiotic_level > 0 的药品）
     */
    @Select("""
            SELECT COUNT(DISTINCT p.id) FROM biz_prescription p
            WHERE p.del_flag = 0
              AND p.prescription_source IN (1, 2)
              AND p.prescription_status IN (3, 4)
              AND p.visit_date BETWEEN #{from} AND #{to}
              AND (#{deptId} IS NULL OR p.dept_id = #{deptId})
              AND EXISTS (SELECT 1 FROM biz_prescription_detail pd
                          JOIN sys_drug d ON d.id = pd.drug_id AND d.del_flag = 0 AND d.antibiotic_level > 0
                          WHERE pd.del_flag = 0 AND pd.prescription_id = p.id)
            """)
    long countOpAbxRx(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("deptId") Long deptId);

    /**
     * 同期出院患者数（按出院日期归月）
     */
    @Select("""
            SELECT COUNT(*) FROM biz_admission a
            WHERE a.del_flag = 0
              AND a.discharge_time IS NOT NULL
              AND DATE(a.discharge_time) BETWEEN #{from} AND #{to}
              AND (#{deptId} IS NULL OR a.dept_id = #{deptId})
            """)
    long countIpDischarge(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("deptId") Long deptId);

    /**
     * 收治患者人天数（同期出院患者的住院天数之和；不足 1 天按 1 天计）
     */
    @Select("""
            SELECT COALESCE(SUM(CASE WHEN DATEDIFF(a.discharge_time, a.admit_time) < 1
                                    THEN 1 ELSE DATEDIFF(a.discharge_time, a.admit_time) END), 0)
            FROM biz_admission a
            WHERE a.del_flag = 0
              AND a.discharge_time IS NOT NULL
              AND DATE(a.discharge_time) BETWEEN #{from} AND #{to}
              AND (#{deptId} IS NULL OR a.dept_id = #{deptId})
            """)
    long sumPatientDays(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("deptId") Long deptId);

    /**
     * 出院患者中使用抗菌药物的人数（住院药品医嘱命中抗菌药物目录）
     */
    @Select("""
            SELECT COUNT(DISTINCT a.admission_id) FROM biz_admission a
            WHERE a.del_flag = 0
              AND a.discharge_time IS NOT NULL
              AND DATE(a.discharge_time) BETWEEN #{from} AND #{to}
              AND (#{deptId} IS NULL OR a.dept_id = #{deptId})
              AND EXISTS (SELECT 1 FROM biz_inpatient_order o
                          LEFT JOIN biz_antibiotic_alias al ON al.alias_name = o.item_name
                          JOIN sys_drug d ON d.del_flag = 0 AND d.antibiotic_level > 0
                               AND (d.drug_code = o.item_code OR d.id = al.drug_id
                                    OR d.drug_name = o.item_name
                                    OR (d.generic_name IS NOT NULL AND d.generic_name = o.item_name))
                          WHERE o.del_flag = 0 AND o.order_class = 1 AND o.order_status <> 6
                            AND o.admission_id = a.admission_id)
            """)
    long countIpAbxPatient(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("deptId") Long deptId);

    /**
     * 住院抗菌药物累计 DDD 数 = Σ(数量 × 每单位含药克数 ÷ WHO DDD)。
     *
     * <p>只算住院医嘱（AUD 是"每 100 人天"的住院强度指标，分子分母必须同源）。
     * 门诊抗菌药消耗不进 AUD —— 这条口径在监测页口径说明里写死，不许拿它对外报门诊强度。
     */
    @Select("""
            SELECT COALESCE(SUM(CASE WHEN d.ddd_value IS NULL OR d.ddd_value <= 0
                                       OR d.ddd_unit_gram IS NULL
                                     THEN 0
                                     ELSE o.quantity * d.ddd_unit_gram / d.ddd_value END), 0)
            FROM biz_inpatient_order o
            JOIN biz_admission a ON a.admission_id = o.admission_id AND a.del_flag = 0
            LEFT JOIN biz_antibiotic_alias al ON al.alias_name = o.item_name
            JOIN sys_drug d ON d.del_flag = 0 AND d.antibiotic_level > 0
                 AND (d.drug_code = o.item_code OR d.id = al.drug_id
                      OR d.drug_name = o.item_name
                      OR (d.generic_name IS NOT NULL AND d.generic_name = o.item_name))
            WHERE o.del_flag = 0 AND o.order_class = 1 AND o.order_status <> 6
              AND a.discharge_time IS NOT NULL
              AND DATE(a.discharge_time) BETWEEN #{from} AND #{to}
              AND (#{deptId} IS NULL OR a.dept_id = #{deptId})
            """)
    BigDecimal sumIpDdds(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("deptId") Long deptId);

    /**
     * 使用抗菌药物的出院患者中，同次住院内送检过微生物标本的人数。
     *
     * <p><b>简化口径</b>：只判"住院期间是否有微生物送检"，不做"送检时刻早于首剂时刻"的严格时序比对。
     * 严格口径需要医嘱执行记录与采样时间对齐，本库住院医嘱与检验采样时点可比但要跨模块拼，
     * 留待下期。这一条必须写在监测页口径说明里 —— 不能拿它当严格口径对外报数。
     */
    @Select("""
            SELECT COUNT(DISTINCT a.admission_id) FROM biz_admission a
            WHERE a.del_flag = 0
              AND a.discharge_time IS NOT NULL
              AND DATE(a.discharge_time) BETWEEN #{from} AND #{to}
              AND (#{deptId} IS NULL OR a.dept_id = #{deptId})
              AND EXISTS (SELECT 1 FROM biz_inpatient_order o
                          LEFT JOIN biz_antibiotic_alias al ON al.alias_name = o.item_name
                          JOIN sys_drug d ON d.del_flag = 0 AND d.antibiotic_level > 0
                               AND (d.drug_code = o.item_code OR d.id = al.drug_id
                                    OR d.drug_name = o.item_name
                                    OR (d.generic_name IS NOT NULL AND d.generic_name = o.item_name))
                          WHERE o.del_flag = 0 AND o.order_class = 1 AND o.order_status <> 6
                            AND o.admission_id = a.admission_id)
              AND EXISTS (SELECT 1 FROM biz_laboratory_apply la
                          JOIN sys_laboratory_item li ON li.id = la.laboratory_item_id AND li.del_flag = 0
                          WHERE la.del_flag = 0 AND la.apply_status <> 6
                            AND li.item_type = 5
                            AND la.patient_id = a.patient_id
                            AND la.submit_time BETWEEN a.admit_time AND a.discharge_time)
            """)
    long countMicroSubmit(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("deptId") Long deptId);

    /**
     * 未解析到药品目录的住院药品医嘱条数（数据质量提示）。
     * > 0 说明有医嘱名没维护进药品字典 / 别名表，这部分消耗量**没有**计入使用强度。
     */
    @Select("""
            SELECT COUNT(*) FROM biz_inpatient_order o
            JOIN biz_admission a ON a.admission_id = o.admission_id AND a.del_flag = 0
            LEFT JOIN biz_antibiotic_alias al ON al.alias_name = o.item_name
            LEFT JOIN sys_drug d ON d.del_flag = 0
                 AND (d.drug_code = o.item_code OR d.id = al.drug_id
                      OR d.drug_name = o.item_name
                      OR (d.generic_name IS NOT NULL AND d.generic_name = o.item_name))
            WHERE o.del_flag = 0 AND o.order_class = 1 AND o.order_status <> 6
              AND d.id IS NULL
              AND a.discharge_time IS NOT NULL
              AND DATE(a.discharge_time) BETWEEN #{from} AND #{to}
              AND (#{deptId} IS NULL OR a.dept_id = #{deptId})
            """)
    long countUnmatchedOrders(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("deptId") Long deptId);

    /**
     * 统计期内有出院患者的科室（scopeType=2 按科室生成时用）
     */
    @Select("""
            SELECT DISTINCT a.dept_id AS deptId,
                   COALESCE(dp.dept_name, CONCAT('科室', a.dept_id)) AS deptName
            FROM biz_admission a
            LEFT JOIN sys_department dp ON dp.id = a.dept_id AND dp.del_flag = 0
            WHERE a.del_flag = 0
              AND a.discharge_time IS NOT NULL
              AND a.dept_id IS NOT NULL
              AND DATE(a.discharge_time) BETWEEN #{from} AND #{to}
            ORDER BY a.dept_id
            """)
    List<MonitorDeptRowVO> selectDischargeDepts(@Param("from") LocalDate from, @Param("to") LocalDate to);

    /**
     * 待点评的 I 类切口手术（已完成、切口等级 1、尚未建点评）。
     * 手术申请单属 his-patient 模块 —— 跨模块裸 SQL 快照，不反向依赖。
     */
    @Select("""
            SELECT oa.id AS operationApplyId, oa.apply_no AS applyNo, oa.admission_id AS admissionId,
                   oa.patient_id AS patientId, oa.patient_name AS patientName,
                   oa.apply_dept_name AS deptName,
                   COALESCE(oa.actual_operation_name, oa.planned_operation_name) AS operationName,
                   COALESCE(oa.actual_operation_code, oa.planned_operation_code) AS operationCode,
                   oa.operation_start_time AS operationTime, oa.surgeon_name AS surgeonName
            FROM biz_operation_apply oa
            WHERE oa.del_flag = 0
              AND oa.incision_level = 1
              AND oa.operation_status = 3
              AND NOT EXISTS (SELECT 1 FROM biz_antibiotic_incision_review r
                              WHERE r.operation_apply_id = oa.id)
            ORDER BY oa.operation_start_time DESC
            LIMIT #{limit}
            """)
    List<IncisionCandidateVO> selectIncisionCandidates(@Param("limit") int limit);

    /**
     * 某台手术围手术期的抗菌药物医嘱候选（手术前后各 24 小时内）。
     * 给药时机由点评人据 operationTime 与医嘱 start_time 判定 —— 系统给证据，不下结论。
     */
    @Select("""
            SELECT o.id AS orderId, d.id AS drugId, d.drug_name AS drugName,
                   d.antibiotic_level AS antibioticLevel, o.item_name AS itemName,
                   o.quantity AS quantity, o.unit AS unit, o.start_time AS startTime,
                   o.order_status AS orderStatus
            FROM biz_inpatient_order o
            LEFT JOIN biz_antibiotic_alias al ON al.alias_name = o.item_name
            JOIN sys_drug d ON d.del_flag = 0 AND d.antibiotic_level > 0
                 AND (d.drug_code = o.item_code OR d.id = al.drug_id
                      OR d.drug_name = o.item_name
                      OR (d.generic_name IS NOT NULL AND d.generic_name = o.item_name))
            WHERE o.del_flag = 0 AND o.order_class = 1 AND o.order_status <> 6
              AND o.admission_id = #{admissionId}
              AND o.start_time BETWEEN DATE_SUB(#{operationTime}, INTERVAL 24 HOUR)
                                   AND DATE_ADD(#{operationTime}, INTERVAL 24 HOUR)
            ORDER BY o.start_time
            """)
    List<IncisionDrugCandidateVO> selectPeriopAntibioticOrders(@Param("admissionId") Long admissionId,
                                                               @Param("operationTime") LocalDateTime operationTime);
}

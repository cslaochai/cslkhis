package com.his.medicaltech.mapper;

import com.his.medicaltech.vo.QualityIssueVO;
import com.his.medicaltech.vo.QualityRuleTotalVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 数据质量取数（P5.3）。
 */
@Mapper
public interface QualityMapper {

    // 一、分母 / 命中：一条 SQL 拿全部规则的 checked 与 issue，供总览页使用
    @Select("""
            SELECT t.ruleCode                                   AS ruleCode,
                   CAST(SUM(t.checkedTotal) AS SIGNED)          AS checkedTotal,
                   CAST(SUM(t.hitTotal) AS SIGNED)              AS hitTotal
            FROM (
                SELECT 'PT-IDENTITY-MISS' AS ruleCode,
                       (SELECT COUNT(*) FROM biz_patient WHERE del_flag = 0) AS checkedTotal,
                       (SELECT COUNT(*) FROM biz_patient WHERE del_flag = 0 AND (
                           id_card IS NULL OR TRIM(id_card) = '' OR address IS NULL OR TRIM(address) = ''
                           OR birth_date IS NULL)) AS hitTotal
                UNION ALL SELECT 'IPR-ADMIT-DOC-MISS',
                       (SELECT COUNT(*) FROM biz_admission WHERE del_flag = 0),
                       (SELECT COUNT(*) FROM biz_admission a WHERE a.del_flag = 0 AND NOT EXISTS (
                           SELECT 1 FROM biz_inpatient_record r WHERE r.del_flag = 0
                             AND r.admission_id = a.admission_id AND r.record_type IN (1, 2)))
                UNION ALL SELECT 'ADM-DISCHARGE-DOC-MISS',
                       (SELECT COUNT(*) FROM biz_admission WHERE del_flag = 0 AND admit_status = 0),
                       (SELECT COUNT(*) FROM biz_admission a WHERE a.del_flag = 0 AND a.admit_status = 0 AND NOT EXISTS (
                           SELECT 1 FROM biz_discharge d WHERE d.del_flag = 0 AND d.admission_id = a.admission_id))
                UNION ALL SELECT 'RX-DETAIL-MISS',
                       (SELECT COUNT(*) FROM biz_prescription WHERE del_flag = 0),
                       (SELECT COUNT(*) FROM biz_prescription p WHERE p.del_flag = 0 AND NOT EXISTS (
                           SELECT 1 FROM biz_prescription_detail d WHERE d.del_flag = 0 AND d.prescription_id = p.id))
                UNION ALL SELECT 'LAB-APPLY-REC-MISS',
                       (SELECT COUNT(*) FROM biz_laboratory_apply WHERE del_flag = 0),
                       (SELECT COUNT(*) FROM biz_laboratory_apply a WHERE a.del_flag = 0 AND NOT EXISTS (
                           SELECT 1 FROM biz_laboratory_record r WHERE r.del_flag = 0 AND r.apply_id = a.id))
                UNION ALL SELECT 'MR-DIAG-CODE-MISS',
                       (SELECT COUNT(*) FROM biz_medical_record WHERE del_flag = 0),
                       (SELECT COUNT(*) FROM biz_medical_record m WHERE m.del_flag = 0
                           AND m.diagnosis_name IS NOT NULL AND TRIM(m.diagnosis_name) <> ''
                           AND (m.diagnosis_code IS NULL OR TRIM(m.diagnosis_code) = ''))
                UNION ALL SELECT 'CHARGE-DISCOUNT-ALLOC-MISS',
                       (SELECT COUNT(*) FROM biz_settlement_bill WHERE del_flag = 0 AND discount_amount > 0),
                       (SELECT COUNT(*) FROM biz_settlement_bill sb WHERE sb.del_flag = 0 AND sb.discount_amount > 0
                           AND NOT EXISTS (SELECT 1 FROM biz_settlement_bill_item i WHERE i.del_flag = 0
                             AND i.bill_id = sb.id AND i.discount_amount > 0))
                UNION ALL SELECT 'ALLERGY-DUAL-MISS',
                       (SELECT COUNT(*) FROM biz_patient WHERE del_flag = 0
                           AND allergy_history IS NOT NULL AND TRIM(allergy_history) <> '' AND allergy_history <> '无'),
                       (SELECT COUNT(*) FROM biz_patient p WHERE p.del_flag = 0
                           AND p.allergy_history IS NOT NULL AND TRIM(p.allergy_history) <> '' AND p.allergy_history <> '无'
                           AND NOT EXISTS (SELECT 1 FROM biz_patient_allergy a WHERE a.patient_id = p.id AND a.del_flag = 0))
                UNION ALL SELECT 'CHARGE-SUM-MISMATCH',
                       (SELECT COUNT(*) FROM biz_settlement_bill WHERE del_flag = 0 AND bill_status <> 4),
                       (SELECT COUNT(*) FROM biz_settlement_bill sb WHERE sb.del_flag = 0 AND sb.bill_status <> 4
                           AND (ABS(sb.total_amount - IFNULL((SELECT SUM(i.amount) FROM biz_settlement_bill_item i
                                 WHERE i.del_flag = 0 AND i.bill_id = sb.id), 0)) > 0.01
                             OR sb.fee_count <> (SELECT COUNT(*) FROM biz_settlement_bill_item i2
                                 WHERE i2.del_flag = 0 AND i2.bill_id = sb.id)
                             OR ABS(IFNULL(sb.paid_amount, 0) - IFNULL((SELECT SUM(t.amount) FROM biz_payment_txn t
                                 WHERE t.del_flag = 0 AND t.txn_status = 1 AND t.bill_id = sb.id), 0)) > 0.01))
                UNION ALL SELECT 'CRITICAL-OVERDUE-HANDLE',
                       (SELECT COUNT(*) FROM biz_critical_value WHERE del_flag = 0),
                       (SELECT COUNT(*) FROM biz_critical_value WHERE del_flag = 0 AND status IN (1, 2)
                           AND deadline_time IS NOT NULL AND deadline_time < NOW())
                UNION ALL SELECT 'CRITICAL-NOTIFY-MISS',
                       (SELECT COUNT(*) FROM biz_critical_value WHERE del_flag = 0 AND status <> 4),
                       (SELECT COUNT(*) FROM biz_critical_value WHERE del_flag = 0 AND status <> 4 AND notify_status = 0)
                UNION ALL SELECT 'ADM-DOC-LATE',
                       (SELECT COUNT(*) FROM biz_admission WHERE del_flag = 0 AND admit_time < DATE_SUB(NOW(), INTERVAL 24 HOUR)),
                       (SELECT COUNT(*) FROM biz_admission a WHERE a.del_flag = 0
                           AND a.admit_time < DATE_SUB(NOW(), INTERVAL 24 HOUR) AND NOT EXISTS (
                             SELECT 1 FROM biz_inpatient_record r WHERE r.del_flag = 0 AND r.admission_id = a.admission_id
                               AND r.record_type IN (1, 2) AND r.create_time <= DATE_ADD(a.admit_time, INTERVAL 24 HOUR)))
                UNION ALL SELECT 'DISCHARGE-ARCHIVE-LATE',
                       (SELECT COUNT(*) FROM biz_admission a JOIN biz_discharge d ON d.admission_id = a.admission_id AND d.del_flag = 0
                           WHERE a.del_flag = 0 AND d.discharge_time < DATE_SUB(NOW(), INTERVAL 7 DAY)),
                       (SELECT COUNT(*) FROM biz_admission a JOIN biz_discharge d ON d.admission_id = a.admission_id AND d.del_flag = 0
                           WHERE a.del_flag = 0 AND d.discharge_time < DATE_SUB(NOW(), INTERVAL 7 DAY) AND NOT EXISTS (
                             SELECT 1 FROM biz_inpatient_record r WHERE r.del_flag = 0 AND r.admission_id = a.admission_id
                               AND r.record_type = 7 AND r.record_status = 3))
                UNION ALL SELECT 'LAB-AUDIT-LATE',
                       (SELECT COUNT(*) FROM biz_laboratory_record WHERE del_flag = 0 AND record_status IN (5, 6, 7)),
                       (SELECT COUNT(*) FROM biz_laboratory_record WHERE del_flag = 0 AND record_status IN (5, 6)
                           AND execute_time IS NOT NULL AND execute_time < DATE_SUB(NOW(), INTERVAL 24 HOUR))
                UNION ALL SELECT 'IPR-KEY-DOC-DUP',
                       (SELECT COUNT(*) FROM biz_admission WHERE del_flag = 0),
                       (SELECT COALESCE(SUM(n - 1), 0) FROM (SELECT COUNT(*) n FROM biz_inpatient_record
                           WHERE del_flag = 0 AND record_type IN (1, 2, 7)
                           GROUP BY admission_id, record_type HAVING COUNT(*) > 1) x)
                UNION ALL SELECT 'LAB-RESULT-DUP',
                       (SELECT COUNT(*) FROM biz_lab_result WHERE del_flag = 0),
                       (SELECT COALESCE(SUM(n - 1), 0) FROM (SELECT COUNT(*) n FROM biz_lab_result
                           WHERE del_flag = 0 GROUP BY record_id, laboratory_item_code HAVING COUNT(*) > 1) x)
                UNION ALL SELECT 'PT-IDCARD-DUP',
                       (SELECT COUNT(*) FROM biz_patient WHERE del_flag = 0 AND id_card IS NOT NULL AND TRIM(id_card) <> ''),
                       (SELECT COALESCE(SUM(n - 1), 0) FROM (SELECT COUNT(*) n FROM biz_patient
                           WHERE del_flag = 0 AND id_card IS NOT NULL AND TRIM(id_card) <> ''
                           GROUP BY id_card HAVING COUNT(*) > 1) x)
                UNION ALL SELECT 'REGIST-DUP-SAME-DAY',
                       (SELECT COUNT(*) FROM biz_appoint_info WHERE del_flag = 0 AND regist_status <> 5),
                       (SELECT COALESCE(SUM(n - 1), 0) FROM (SELECT COUNT(*) n FROM biz_appoint_info
                           WHERE del_flag = 0 AND regist_status <> 5
                           GROUP BY patient_id, dept_id, visit_date HAVING COUNT(*) > 1) x)
                UNION ALL SELECT 'GENDER-IDCARD-CONFLICT',
                       (SELECT COUNT(*) FROM biz_patient WHERE del_flag = 0 AND TRIM(id_card) REGEXP '^[0-9]{17}[0-9Xx]$'),
                       (SELECT COUNT(*) FROM biz_patient WHERE del_flag = 0 AND TRIM(id_card) REGEXP '^[0-9]{17}[0-9Xx]$'
                           AND ((gender = 1 AND CAST(SUBSTRING(TRIM(id_card), 17, 1) AS UNSIGNED) % 2 = 0)
                             OR (gender = 2 AND CAST(SUBSTRING(TRIM(id_card), 17, 1) AS UNSIGNED) % 2 = 1)))
                UNION ALL SELECT 'IDCARD-FORMAT-INVALID',
                       (SELECT COUNT(*) FROM biz_patient WHERE del_flag = 0 AND id_card IS NOT NULL AND TRIM(id_card) <> ''),
                       (SELECT COUNT(*) FROM biz_patient WHERE del_flag = 0 AND id_card IS NOT NULL AND TRIM(id_card) <> ''
                           AND TRIM(id_card) NOT REGEXP '^[0-9]{17}[0-9Xx]$')
                UNION ALL SELECT 'CODE-VALUE-INVALID',
                       (SELECT (SELECT COUNT(*) FROM biz_patient WHERE del_flag = 0)
                             + (SELECT COUNT(*) FROM biz_inpatient_record WHERE del_flag = 0)
                             + (SELECT COUNT(*) FROM biz_emergency WHERE del_flag = 0)
                             + (SELECT COUNT(*) FROM biz_settlement_bill
                                WHERE del_flag = 0 AND encounter_type = 2 AND bill_type = 4)),
                       (SELECT (SELECT COUNT(*) FROM biz_patient WHERE del_flag = 0 AND gender NOT IN (1, 2, 3))
                             + (SELECT COUNT(*) FROM biz_inpatient_record WHERE del_flag = 0
                                 AND record_type NOT IN (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11))
                             + (SELECT COUNT(*) FROM biz_emergency WHERE del_flag = 0
                                 AND (triage_level IS NULL OR triage_level NOT BETWEEN 1 AND 4))
                             + (SELECT COUNT(*) FROM biz_settlement_bill WHERE del_flag = 0
                                 AND encounter_type = 2 AND bill_type = 4
                                 AND bill_status NOT IN (1, 2, 3, 4, 5)))
                UNION ALL SELECT 'DATE-REVERSE',
                       (SELECT COUNT(*) FROM biz_admission WHERE del_flag = 0 AND discharge_time IS NOT NULL),
                       (SELECT COUNT(*) FROM biz_admission WHERE del_flag = 0 AND discharge_time IS NOT NULL
                           AND discharge_time < admit_time)
                UNION ALL SELECT 'AMOUNT-INVALID',
                       (SELECT COUNT(*) FROM biz_fee_record WHERE del_flag = 0),
                       (SELECT COUNT(*) FROM biz_fee_record WHERE del_flag = 0
                           AND (price < 0 OR quantity = 0 OR amount = 0 OR SIGN(amount) <> SIGN(quantity)))
            ) t
            GROUP BY t.ruleCode
            """)
    List<QualityRuleTotalVO> selectRuleTotals();

    // 二、明细：每条规则一个方法，必须能定位到具体行

    @Select("""
            SELECT 'PT-IDENTITY-MISS'   AS ruleCode,
                   'biz_patient'        AS tableName,
                   p.id   AS recordId,
                   p.patient_no         AS recordNo,
                   p.id   AS patientId,
                   p.patient_no         AS patientNo,
                   p.patient_name       AS patientName,
                   ''                   AS deptName,
                   ''                   AS doctorName,
                   CONCAT('缺少 ', CONCAT_WS('、',
                       IF(p.id_card IS NULL OR TRIM(p.id_card) = '', '身份证号', NULL),
                       IF(p.address IS NULL OR TRIM(p.address) = '', '家庭住址', NULL),
                       IF(p.birth_date IS NULL, '出生日期', NULL))) AS detail,
                   IFNULL(DATE_FORMAT(p.create_time, '%Y-%m-%d %H:%i:%s'), '') AS occurredTime
            FROM biz_patient p
            WHERE p.del_flag = 0 AND (p.id_card IS NULL OR TRIM(p.id_card) = ''
                  OR p.address IS NULL OR TRIM(p.address) = '' OR p.birth_date IS NULL)
            ORDER BY p.create_time DESC, p.id DESC
            """)
    List<QualityIssueVO> rulePtIdentityMiss();

    @Select("""
            SELECT 'IPR-ADMIT-DOC-MISS'  AS ruleCode,
                   'biz_admission'       AS tableName,
                   a.admission_id AS recordId,
                   a.admission_no        AS recordNo,
                   a.patient_id   AS patientId,
                   ''                    AS patientNo,
                   (SELECT p.patient_name FROM biz_patient p WHERE p.id = a.patient_id) AS patientName,
                   ''                    AS deptName,
                   ''                    AS doctorName,
                   CONCAT('住院 ', DATE_FORMAT(a.admit_time, '%Y-%m-%d %H:%i'), ' 入院，',
                       IFNULL(a.diagnosis, '（未填入院诊断）'),
                       '；未找到入院记录 / 首次病程文书') AS detail,
                   IFNULL(DATE_FORMAT(a.admit_time, '%Y-%m-%d %H:%i:%s'), '') AS occurredTime
            FROM biz_admission a
            WHERE a.del_flag = 0 AND NOT EXISTS (
                  SELECT 1 FROM biz_inpatient_record r WHERE r.del_flag = 0
                    AND r.admission_id = a.admission_id AND r.record_type IN (1, 2))
            ORDER BY a.admit_time DESC
            """)
    List<QualityIssueVO> ruleIprAdmitDocMiss();

    @Select("""
            SELECT 'ADM-DISCHARGE-DOC-MISS' AS ruleCode,
                   'biz_admission'          AS tableName,
                   a.admission_id AS recordId,
                   a.admission_no           AS recordNo,
                   a.patient_id   AS patientId,
                   ''                       AS patientNo,
                   (SELECT p.patient_name FROM biz_patient p WHERE p.id = a.patient_id) AS patientName,
                   ''                    AS deptName,
                   ''                    AS doctorName,
                   CONCAT('状态已是「已出院」',
                       IF(a.discharge_time IS NULL, '（但出院时间为空）',
                          CONCAT('，出院时间 ', DATE_FORMAT(a.discharge_time, '%Y-%m-%d %H:%i'))),
                       '，却没有出院记录') AS detail,
                   IFNULL(DATE_FORMAT(a.discharge_time, '%Y-%m-%d %H:%i:%s'), '') AS occurredTime
            FROM biz_admission a
            WHERE a.del_flag = 0 AND a.admit_status = 0 AND NOT EXISTS (
                  SELECT 1 FROM biz_discharge d WHERE d.del_flag = 0 AND d.admission_id = a.admission_id)
            ORDER BY a.discharge_time DESC
            """)
    List<QualityIssueVO> ruleAdmDischargeDocMiss();

    @Select("""
            SELECT 'RX-DETAIL-MISS'        AS ruleCode,
                   'biz_prescription'      AS tableName,
                   p.id      AS recordId,
                   p.prescription_no       AS recordNo,
                   p.patient_id AS patientId,
                   p.patient_no            AS patientNo,
                   p.patient_name          AS patientName,
                   p.dept_name             AS deptName,
                   p.doctor_name           AS doctorName,
                   CONCAT('处方 ', IFNULL(p.prescription_no, ''),
                       '（', IFNULL(p.diagnosis, '未填诊断'), '）没有明细行；核定金额 ',
                       IFNULL(p.total_amount, 0), ' 元') AS detail,
                   IFNULL(DATE_FORMAT(p.create_time, '%Y-%m-%d %H:%i:%s'), '') AS occurredTime
            FROM biz_prescription p
            WHERE p.del_flag = 0 AND NOT EXISTS (
                  SELECT 1 FROM biz_prescription_detail d WHERE d.del_flag = 0 AND d.prescription_id = p.id)
            ORDER BY p.create_time DESC
            """)
    List<QualityIssueVO> ruleRxDetailMiss();

    @Select("""
            SELECT 'LAB-APPLY-REC-MISS'    AS ruleCode,
                   'biz_laboratory_apply'  AS tableName,
                   a.id      AS recordId,
                   a.apply_no              AS recordNo,
                   a.patient_id AS patientId,
                   a.patient_no            AS patientNo,
                   a.patient_name          AS patientName,
                   a.dept_name             AS deptName,
                   a.doctor_name           AS doctorName,
                   CONCAT('检验申请 ', IFNULL(a.apply_no, ''), '（',
                       IFNULL(a.laboratory_item_name, '未填项目'), '）未见检验记录') AS detail,
                   IFNULL(DATE_FORMAT(a.create_time, '%Y-%m-%d %H:%i:%s'), '') AS occurredTime
            FROM biz_laboratory_apply a
            WHERE a.del_flag = 0 AND NOT EXISTS (
                  SELECT 1 FROM biz_laboratory_record r WHERE r.del_flag = 0 AND r.apply_id = a.id)
            ORDER BY a.create_time DESC
            """)
    List<QualityIssueVO> ruleLabApplyRecMiss();

    @Select("""
            SELECT 'MR-DIAG-CODE-MISS'     AS ruleCode,
                   'biz_medical_record'    AS tableName,
                   m.id      AS recordId,
                   m.record_no             AS recordNo,
                   m.patient_id AS patientId,
                   m.patient_no            AS patientNo,
                   m.patient_name          AS patientName,
                   m.dept_name             AS deptName,
                   m.doctor_name           AS doctorName,
                   CONCAT('诊断名「', IFNULL(m.diagnosis_name, ''), '」没有对应的 ICD 编码') AS detail,
                   IFNULL(DATE_FORMAT(m.create_time, '%Y-%m-%d %H:%i:%s'), '') AS occurredTime
            FROM biz_medical_record m
            WHERE m.del_flag = 0 AND m.diagnosis_name IS NOT NULL AND TRIM(m.diagnosis_name) <> ''
                  AND (m.diagnosis_code IS NULL OR TRIM(m.diagnosis_code) = '')
            ORDER BY m.create_time DESC
            """)
    List<QualityIssueVO> ruleMrDiagCodeMiss();

    @Select("""
            SELECT 'CHARGE-DISCOUNT-ALLOC-MISS' AS ruleCode,
                   'biz_settlement_bill'        AS tableName,
                   sb.id          AS recordId,
                   sb.bill_no                   AS recordNo,
                   sb.patient_id  AS patientId,
                   sb.patient_no                AS patientNo,
                   sb.patient_name              AS patientName,
                   ''                           AS deptName,
                   ''                           AS doctorName,
                   CONCAT('结算账单 ', IFNULL(sb.bill_no, ''), ' 单头优惠 ', sb.discount_amount,
                       ' 元，但 ',
                       (SELECT COUNT(*) FROM biz_settlement_bill_item i WHERE i.del_flag = 0 AND i.bill_id = sb.id),
                       ' 个账单行的分摊优惠全为 0（统筹在 pool_amount，不属优惠）') AS detail,
                   IFNULL(DATE_FORMAT(sb.bill_time, '%Y-%m-%d %H:%i:%s'), '') AS occurredTime
            FROM biz_settlement_bill sb
            WHERE sb.del_flag = 0 AND sb.discount_amount > 0 AND NOT EXISTS (
                  SELECT 1 FROM biz_settlement_bill_item i WHERE i.del_flag = 0
                    AND i.bill_id = sb.id AND i.discount_amount > 0)
            ORDER BY sb.bill_time DESC
            """)
    List<QualityIssueVO> ruleChargeDiscountAllocMiss();

    @Select("""
            SELECT 'ALLERGY-DUAL-MISS'     AS ruleCode,
                   'biz_patient'           AS tableName,
                   p.id      AS recordId,
                   p.patient_no            AS recordNo,
                   p.id      AS patientId,
                   p.patient_no            AS patientNo,
                   p.patient_name          AS patientName,
                   ''                      AS deptName,
                   ''                      AS doctorName,
                   CONCAT('患者主档过敏史为「', IFNULL(p.allergy_history, ''), '」，',
                       '但结构化过敏表（biz_patient_allergy）中没有任何记录 —— 用药审核读的是结构化表') AS detail,
                   IFNULL(DATE_FORMAT(p.update_time, '%Y-%m-%d %H:%i:%s'), '') AS occurredTime
            FROM biz_patient p
            WHERE p.del_flag = 0 AND p.allergy_history IS NOT NULL AND TRIM(p.allergy_history) <> ''
                  AND p.allergy_history <> '无' AND NOT EXISTS (
                    SELECT 1 FROM biz_patient_allergy a WHERE a.patient_id = p.id AND a.del_flag = 0)
            ORDER BY p.id DESC
            """)
    List<QualityIssueVO> ruleAllergyDualMiss();

    /**
     * 四层互对：账单应收 = 账单行合计、fee_count = 行条数、单头已收 = 收款流水净额
     * （流水一笔一行带符号，收正退负一起 SUM，所以退款会把已收往回冲）。
     */
    @Select("""
            SELECT 'CHARGE-SUM-MISMATCH'   AS ruleCode,
                   'biz_settlement_bill'   AS tableName,
                   sb.id     AS recordId,
                   sb.bill_no              AS recordNo,
                   sb.patient_id AS patientId,
                   sb.patient_no           AS patientNo,
                   sb.patient_name         AS patientName,
                   ''                      AS deptName,
                   ''                      AS doctorName,
                   CONCAT_WS('；',
                       IF(ABS(IFNULL(sb.total_amount, 0) - IFNULL(x.item_sum, 0)) > 0.01,
                          CONCAT('应收 ', IFNULL(sb.total_amount, 0), '，账单行合计 ', IFNULL(x.item_sum, 0),
                                 '，差 ', ROUND(IFNULL(sb.total_amount, 0) - IFNULL(x.item_sum, 0), 2)), NULL),
                       IF(IFNULL(sb.fee_count, 0) <> IFNULL(x.item_count, 0),
                          CONCAT('单头记账行数 ', IFNULL(sb.fee_count, 0), '，实际账单行 ', IFNULL(x.item_count, 0)), NULL),
                       IF(ABS(IFNULL(sb.paid_amount, 0) - IFNULL(y.txn_sum, 0)) > 0.01,
                          CONCAT('单头已收 ', IFNULL(sb.paid_amount, 0), '，收款流水净额 ', IFNULL(y.txn_sum, 0)), NULL)) AS detail,
                   IFNULL(DATE_FORMAT(sb.bill_time, '%Y-%m-%d %H:%i:%s'), '') AS occurredTime
            FROM biz_settlement_bill sb
            LEFT JOIN (SELECT i.bill_id AS bid, COUNT(*) item_count, SUM(i.amount) item_sum
                         FROM biz_settlement_bill_item i WHERE i.del_flag = 0 GROUP BY i.bill_id) x ON x.bid = sb.id
            LEFT JOIN (SELECT t.bill_id AS bid, SUM(t.amount) txn_sum
                         FROM biz_payment_txn t WHERE t.del_flag = 0 AND t.txn_status = 1 GROUP BY t.bill_id) y ON y.bid = sb.id
            WHERE sb.del_flag = 0 AND sb.bill_status <> 4
                  AND (ABS(IFNULL(sb.total_amount, 0) - IFNULL(x.item_sum, 0)) > 0.01
                    OR IFNULL(sb.fee_count, 0) <> IFNULL(x.item_count, 0)
                    OR ABS(IFNULL(sb.paid_amount, 0) - IFNULL(y.txn_sum, 0)) > 0.01)
            ORDER BY sb.bill_time DESC
            """)
    List<QualityIssueVO> ruleChargeSumMismatch();

    @Select("""
            SELECT 'CRITICAL-OVERDUE-HANDLE' AS ruleCode,
                   'biz_critical_value'      AS tableName,
                   c.id        AS recordId,
                   c.critical_no             AS recordNo,
                   c.patient_id AS patientId,
                   c.patient_no              AS patientNo,
                   c.patient_name            AS patientName,
                   IFNULL(c.report_dept_name, '') AS deptName,
                   IFNULL(c.report_by, '')        AS doctorName,
                   CONCAT('危急值 ', IFNULL(c.item_name, ''), ' = ', IFNULL(c.result_value, ''),
                       IFNULL(c.result_unit, ''), '（', IFNULL(c.threshold_text, IFNULL(c.critical_desc, '')), '）',
                       '，时限 ', IFNULL(DATE_FORMAT(c.deadline_time, '%Y-%m-%d %H:%i'), '未设'),
                       '，当前状态 ', CASE c.status WHEN 1 THEN '待接收' WHEN 2 THEN '已接收' ELSE CONCAT('未知(', c.status, ')') END,
                       ' —— 已超时未完成处置') AS detail,
                   IFNULL(DATE_FORMAT(c.report_time, '%Y-%m-%d %H:%i:%s'), '') AS occurredTime
            FROM biz_critical_value c
            WHERE c.del_flag = 0 AND c.status IN (1, 2) AND c.deadline_time IS NOT NULL AND c.deadline_time < NOW()
            ORDER BY c.deadline_time ASC
            """)
    List<QualityIssueVO> ruleCriticalOverdueHandle();

    @Select("""
            SELECT 'CRITICAL-NOTIFY-MISS'  AS ruleCode,
                   'biz_critical_value'    AS tableName,
                   c.id      AS recordId,
                   c.critical_no           AS recordNo,
                   c.patient_id AS patientId,
                   c.patient_no            AS patientNo,
                   c.patient_name          AS patientName,
                   IFNULL(c.report_dept_name, '') AS deptName,
                   IFNULL(c.report_by, '')        AS doctorName,
                   CONCAT('危急值 ', IFNULL(c.item_name, ''), ' = ', IFNULL(c.result_value, ''),
                       IFNULL(c.result_unit, ''), '，报告人 ', IFNULL(c.report_by, '未填'),
                       '，通知状态为「未通知」（闭环状态 ',
                       CASE c.status WHEN 1 THEN '待接收' WHEN 2 THEN '已接收' WHEN 3 THEN '已处置' ELSE CONCAT('未知(', c.status, ')') END,
                       '）') AS detail,
                   IFNULL(DATE_FORMAT(c.report_time, '%Y-%m-%d %H:%i:%s'), '') AS occurredTime
            FROM biz_critical_value c
            WHERE c.del_flag = 0 AND c.status <> 4 AND c.notify_status = 0
            ORDER BY c.report_time DESC
            """)
    List<QualityIssueVO> ruleCriticalNotifyMiss();

    @Select("""
            SELECT 'ADM-DOC-LATE'          AS ruleCode,
                   'biz_admission'         AS tableName,
                   a.admission_id AS recordId,
                   a.admission_no          AS recordNo,
                   a.patient_id   AS patientId,
                   ''                      AS patientNo,
                   (SELECT p.patient_name FROM biz_patient p WHERE p.id = a.patient_id) AS patientName,
                   ''                    AS deptName,
                   ''                    AS doctorName,
                   CONCAT('入院时间 ', DATE_FORMAT(a.admit_time, '%Y-%m-%d %H:%i'),
                       '，已过 ', TIMESTAMPDIFF(HOUR, a.admit_time, NOW()), ' 小时，',
                       '入院记录 / 首次病程仍未在 24 小时内完成') AS detail,
                   IFNULL(DATE_FORMAT(a.admit_time, '%Y-%m-%d %H:%i:%s'), '') AS occurredTime
            FROM biz_admission a
            WHERE a.del_flag = 0 AND a.admit_time < DATE_SUB(NOW(), INTERVAL 24 HOUR) AND NOT EXISTS (
                  SELECT 1 FROM biz_inpatient_record r WHERE r.del_flag = 0 AND r.admission_id = a.admission_id
                    AND r.record_type IN (1, 2) AND r.create_time <= DATE_ADD(a.admit_time, INTERVAL 24 HOUR))
            ORDER BY a.admit_time ASC
            """)
    List<QualityIssueVO> ruleAdmDocLate();

    @Select("""
            SELECT 'DISCHARGE-ARCHIVE-LATE' AS ruleCode,
                   'biz_admission'          AS tableName,
                   a.admission_id AS recordId,
                   a.admission_no           AS recordNo,
                   a.patient_id   AS patientId,
                   ''                       AS patientNo,
                   (SELECT p.patient_name FROM biz_patient p WHERE p.id = a.patient_id) AS patientName,
                   ''                    AS deptName,
                   ''                    AS doctorName,
                   CONCAT('出院时间 ', DATE_FORMAT(d.discharge_time, '%Y-%m-%d %H:%i'),
                       '，已过 ', TIMESTAMPDIFF(DAY, d.discharge_time, NOW()), ' 天，出院记录未归档') AS detail,
                   IFNULL(DATE_FORMAT(d.discharge_time, '%Y-%m-%d %H:%i:%s'), '') AS occurredTime
            FROM biz_admission a
            JOIN biz_discharge d ON d.admission_id = a.admission_id AND d.del_flag = 0
            WHERE a.del_flag = 0 AND d.discharge_time < DATE_SUB(NOW(), INTERVAL 7 DAY) AND NOT EXISTS (
                  SELECT 1 FROM biz_inpatient_record r WHERE r.del_flag = 0 AND r.admission_id = a.admission_id
                    AND r.record_type = 7 AND r.record_status = 3)
            ORDER BY d.discharge_time ASC
            """)
    List<QualityIssueVO> ruleDischargeArchiveLate();

    @Select("""
            SELECT 'LAB-AUDIT-LATE'        AS ruleCode,
                   'biz_laboratory_record' AS tableName,
                   r.id      AS recordId,
                   r.record_no             AS recordNo,
                   r.patient_id AS patientId,
                   r.patient_no            AS patientNo,
                   r.patient_name          AS patientName,
                   r.apply_dept_name       AS deptName,
                   r.execute_by            AS doctorName,
                   CONCAT('检验 ', IFNULL(r.laboratory_item_name, ''), ' 于 ',
                       IFNULL(DATE_FORMAT(r.execute_time, '%Y-%m-%d %H:%i'), '未知时间'), ' 出结果，已过 ',
                       TIMESTAMPDIFF(HOUR, r.execute_time, NOW()), ' 小时仍未审核（当前状态 ',
                       CASE r.record_status WHEN 5 THEN '已出结果' WHEN 6 THEN '已审核' ELSE CONCAT('未知(', r.record_status, ')') END,
                       '）') AS detail,
                   IFNULL(DATE_FORMAT(r.execute_time, '%Y-%m-%d %H:%i:%s'), '') AS occurredTime
            FROM biz_laboratory_record r
            WHERE r.del_flag = 0 AND r.record_status IN (5, 6) AND r.execute_time IS NOT NULL
                  AND r.execute_time < DATE_SUB(NOW(), INTERVAL 24 HOUR)
            ORDER BY r.execute_time ASC
            """)
    List<QualityIssueVO> ruleLabAuditLate();

    @Select("""
            SELECT 'IPR-KEY-DOC-DUP'       AS ruleCode,
                   'biz_inpatient_record'  AS tableName,
                   x.id      AS recordId,
                   x.record_no             AS recordNo,
                   x.patient_id AS patientId,
                   IFNULL(x.patient_no, '') AS patientNo,
                   IFNULL(x.patient_name, '') AS patientName,
                   IFNULL(x.dept_name, '') AS deptName,
                   IFNULL(x.doctor_name, '') AS doctorName,
                   CONCAT('同一次住院（admission_id=', x.admission_id, '）已有 ',
                       (SELECT COUNT(*) FROM biz_inpatient_record r2 WHERE r2.del_flag = 0
                         AND r2.admission_id = x.admission_id AND r2.record_type = x.record_type),
                       ' 份「', CASE x.record_type WHEN 1 THEN '入院记录' WHEN 2 THEN '首次病程' WHEN 7 THEN '出院记录'
                         ELSE CONCAT('类型', x.record_type) END, '」，本条为多余的第 ',
                       (SELECT COUNT(*) FROM biz_inpatient_record r3 WHERE r3.del_flag = 0
                         AND r3.admission_id = x.admission_id AND r3.record_type = x.record_type AND r3.id <= x.id),
                       ' 份') AS detail,
                   IFNULL(DATE_FORMAT(x.create_time, '%Y-%m-%d %H:%i:%s'), '') AS occurredTime
            FROM (SELECT r.*, ROW_NUMBER() OVER (PARTITION BY r.admission_id, r.record_type ORDER BY r.id) rn
                  FROM biz_inpatient_record r
                  WHERE r.del_flag = 0 AND r.record_type IN (1, 2, 7)) x
            WHERE x.rn > 1
            ORDER BY x.admission_id, x.record_type, x.id
            """)
    List<QualityIssueVO> ruleIprKeyDocDup();

    @Select("""
            SELECT 'LAB-RESULT-DUP'        AS ruleCode,
                   'biz_lab_result'        AS tableName,
                   x.id      AS recordId,
                   IFNULL(x.record_no, '') AS recordNo,
                   ''                      AS patientId,
                   ''                      AS patientNo,
                   IFNULL((SELECT r.patient_name FROM biz_laboratory_record r WHERE r.id = x.record_id), '') AS patientName,
                   ''                      AS deptName,
                   ''                      AS doctorName,
                   CONCAT('检验记录 ', IFNULL(x.record_no, ''), ' 的「', IFNULL(x.laboratory_item_name, ''),
                       '」有 ',
                       (SELECT COUNT(*) FROM biz_lab_result r2 WHERE r2.del_flag = 0
                         AND r2.record_id = x.record_id AND r2.laboratory_item_code = x.laboratory_item_code),
                       ' 条结果行，本条为多余的第 ',
                       (SELECT COUNT(*) FROM biz_lab_result r3 WHERE r3.del_flag = 0
                         AND r3.record_id = x.record_id AND r3.laboratory_item_code = x.laboratory_item_code AND r3.id <= x.id),
                       ' 条') AS detail,
                   IFNULL(DATE_FORMAT(x.create_time, '%Y-%m-%d %H:%i:%s'), '') AS occurredTime
            FROM (SELECT r.*, ROW_NUMBER() OVER (PARTITION BY r.record_id, r.laboratory_item_code ORDER BY r.id) rn
                  FROM biz_lab_result r WHERE r.del_flag = 0) x
            WHERE x.rn > 1
            ORDER BY x.record_id, x.laboratory_item_code, x.id
            """)
    List<QualityIssueVO> ruleLabResultDup();

    @Select("""
            SELECT 'PT-IDCARD-DUP'         AS ruleCode,
                   'biz_patient'           AS tableName,
                   x.id      AS recordId,
                   x.patient_no            AS recordNo,
                   x.id      AS patientId,
                   x.patient_no            AS patientNo,
                   x.patient_name          AS patientName,
                   ''                      AS deptName,
                   ''                      AS doctorName,
                   CONCAT('身份证 ', CONCAT(LEFT(TRIM(x.id_card), 6), '********', RIGHT(TRIM(x.id_card), 4)),
                       ' 在本库有 ',
                       (SELECT COUNT(*) FROM biz_patient p2 WHERE p2.del_flag = 0 AND TRIM(p2.id_card) = TRIM(x.id_card)),
                       ' 条患者档案，本条为多余的第 ',
                       (SELECT COUNT(*) FROM biz_patient p3 WHERE p3.del_flag = 0 AND TRIM(p3.id_card) = TRIM(x.id_card) AND p3.id <= x.id),
                       ' 条') AS detail,
                   IFNULL(DATE_FORMAT(x.create_time, '%Y-%m-%d %H:%i:%s'), '') AS occurredTime
            FROM (SELECT p.*, ROW_NUMBER() OVER (PARTITION BY TRIM(p.id_card) ORDER BY p.id) rn
                  FROM biz_patient p WHERE p.del_flag = 0 AND p.id_card IS NOT NULL AND TRIM(p.id_card) <> '') x
            WHERE x.rn > 1
            ORDER BY x.id_card, x.id
            """)
    List<QualityIssueVO> rulePtIdcardDup();

    @Select("""
            SELECT 'REGIST-DUP-SAME-DAY'   AS ruleCode,
                   'biz_appoint_info'       AS tableName,
                   x.id      AS recordId,
                   x.regist_no             AS recordNo,
                   x.patient_id AS patientId,
                   x.patient_no            AS patientNo,
                   x.patient_name          AS patientName,
                   x.dept_name             AS deptName,
                   x.doctor_name           AS doctorName,
                   CONCAT('患者于 ', x.visit_date, ' 在「', IFNULL(x.dept_name, ''), '」有 ',
                       (SELECT COUNT(*) FROM biz_appoint_info r2 WHERE r2.del_flag = 0 AND r2.regist_status <> 5
                         AND r2.patient_id = x.patient_id AND r2.dept_id = x.dept_id AND r2.visit_date = x.visit_date),
                       ' 次有效挂号，本条为多余的第 ',
                       (SELECT COUNT(*) FROM biz_appoint_info r3 WHERE r3.del_flag = 0 AND r3.regist_status <> 5
                         AND r3.patient_id = x.patient_id AND r3.dept_id = x.dept_id AND r3.visit_date = x.visit_date
                         AND r3.id <= x.id),
                       ' 次（', IFNULL(x.regist_no, ''), '）') AS detail,
                   IFNULL(DATE_FORMAT(x.regist_time, '%Y-%m-%d %H:%i:%s'), '') AS occurredTime
            FROM (SELECT r.*, ROW_NUMBER() OVER (PARTITION BY r.patient_id, r.dept_id, r.visit_date ORDER BY r.id) rn
                  FROM biz_appoint_info r WHERE r.del_flag = 0 AND r.regist_status <> 5) x
            WHERE x.rn > 1
            ORDER BY x.patient_id, x.visit_date, x.id
            """)
    List<QualityIssueVO> ruleRegistDupSameDay();

    @Select("""
            SELECT 'GENDER-IDCARD-CONFLICT' AS ruleCode,
                   'biz_patient'           AS tableName,
                   p.id      AS recordId,
                   p.patient_no            AS recordNo,
                   p.id      AS patientId,
                   p.patient_no            AS patientNo,
                   p.patient_name          AS patientName,
                   ''                      AS deptName,
                   ''                      AS doctorName,
                   CONCAT('库内性别码 ', p.gender, '（后端枚举 1-男 2-女），身份证 ',
                       CONCAT(LEFT(TRIM(p.id_card), 6), '********', RIGHT(TRIM(p.id_card), 4)),
                       ' 第 17 位为 ', SUBSTRING(TRIM(p.id_card), 17, 1), '（奇=男 偶=女），两者矛盾') AS detail,
                   IFNULL(DATE_FORMAT(p.update_time, '%Y-%m-%d %H:%i:%s'), '') AS occurredTime
            FROM biz_patient p
            WHERE p.del_flag = 0 AND TRIM(p.id_card) REGEXP '^[0-9]{17}[0-9Xx]$'
                  AND ((p.gender = 1 AND CAST(SUBSTRING(TRIM(p.id_card), 17, 1) AS UNSIGNED) % 2 = 0)
                    OR (p.gender = 2 AND CAST(SUBSTRING(TRIM(p.id_card), 17, 1) AS UNSIGNED) % 2 = 1))
            ORDER BY p.id
            """)
    List<QualityIssueVO> ruleGenderIdcardConflict();

    @Select("""
            SELECT 'IDCARD-FORMAT-INVALID' AS ruleCode,
                   'biz_patient'           AS tableName,
                   p.id      AS recordId,
                   p.patient_no            AS recordNo,
                   p.id      AS patientId,
                   p.patient_no            AS patientNo,
                   p.patient_name          AS patientName,
                   ''                      AS deptName,
                   ''                      AS doctorName,
                   CONCAT('身份证号「', TRIM(p.id_card), '」长度 ', CHAR_LENGTH(TRIM(p.id_card)),
                       '，不符合 18 位身份证规则（末位可为 X）') AS detail,
                   IFNULL(DATE_FORMAT(p.update_time, '%Y-%m-%d %H:%i:%s'), '') AS occurredTime
            FROM biz_patient p
            WHERE p.del_flag = 0 AND p.id_card IS NOT NULL AND TRIM(p.id_card) <> ''
                  AND TRIM(p.id_card) NOT REGEXP '^[0-9]{17}[0-9Xx]$'
            ORDER BY p.id
            """)
    List<QualityIssueVO> ruleIdcardFormatInvalid();

    @Select("""
            SELECT 'CODE-VALUE-INVALID' AS ruleCode,
                   'biz_patient'        AS tableName,
                   p.id   AS recordId,
                   p.patient_no         AS recordNo,
                   p.id   AS patientId,
                   p.patient_no         AS patientNo,
                   p.patient_name       AS patientName,
                   ''                   AS deptName,
                   ''                   AS doctorName,
                   CONCAT('患者性别码 = ', p.gender,
                       '，不在枚举 {1-男 2-女 9-未知} 内，前端会渲染为「未知(', p.gender, ')」') AS detail,
                   IFNULL(DATE_FORMAT(p.update_time, '%Y-%m-%d %H:%i:%s'), '') AS occurredTime
            FROM biz_patient p
            WHERE p.del_flag = 0 AND p.gender NOT IN (1, 2, 3)
            UNION ALL
            SELECT 'CODE-VALUE-INVALID', 'biz_inpatient_record',
                   r.id, r.record_no, r.patient_id, IFNULL(r.patient_no, ''),
                   IFNULL(r.patient_name, ''), IFNULL(r.dept_name, ''), IFNULL(r.doctor_name, ''),
                   CONCAT('住院文书类型码 = ', r.record_type,
                       '，不在字典 {1-入院记录 … 11-输血记录} 内，前端会渲染为「未知(', r.record_type, ')」'),
                   IFNULL(DATE_FORMAT(r.create_time, '%Y-%m-%d %H:%i:%s'), '')
            FROM biz_inpatient_record r
            WHERE r.del_flag = 0 AND r.record_type NOT IN (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
            UNION ALL
            SELECT 'CODE-VALUE-INVALID', 'biz_emergency',
                   e.id, e.emergency_no, e.patient_id, IFNULL(e.patient_no, ''),
                   IFNULL(e.patient_name, ''), IFNULL(e.dept_name, ''), IFNULL(e.doctor_name, ''),
                   CONCAT('急诊分诊级别码 = ', IFNULL(e.triage_level, 'NULL'),
                       '，不在字典 {1-Ⅰ级濒危 2-Ⅱ级危重 3-Ⅲ级急症 4-Ⅳ级非急症} 内'),
                   IFNULL(DATE_FORMAT(e.admission_time, '%Y-%m-%d %H:%i:%s'), '')
            FROM biz_emergency e
            WHERE e.del_flag = 0 AND (e.triage_level IS NULL OR e.triage_level NOT BETWEEN 1 AND 4)
            UNION ALL
            SELECT 'CODE-VALUE-INVALID', 'biz_settlement_bill',
                   s.id, s.bill_no, s.patient_id, IFNULL(s.patient_no, ''),
                   IFNULL(s.patient_name, ''), '', '',
                   CONCAT('账单状态码 = ', s.bill_status, '，不在字典 {1-待支付 2-部分支付 3-已支付 4-已作废 5-已退费} 内'),
                   IFNULL(DATE_FORMAT(s.bill_time, '%Y-%m-%d %H:%i:%s'), '')
            FROM biz_settlement_bill s
            WHERE s.del_flag = 0 AND s.encounter_type = 2 AND s.bill_type = 4
              AND s.bill_status NOT IN (1, 2, 3, 4, 5)
            """)
    List<QualityIssueVO> ruleCodeValueInvalid();

    @Select("""
            SELECT 'DATE-REVERSE'          AS ruleCode,
                   'biz_admission'         AS tableName,
                   a.admission_id AS recordId,
                   a.admission_no          AS recordNo,
                   a.patient_id   AS patientId,
                   ''                      AS patientNo,
                   (SELECT p.patient_name FROM biz_patient p WHERE p.id = a.patient_id) AS patientName,
                   ''                    AS deptName,
                   ''                    AS doctorName,
                   CONCAT('入院 ', DATE_FORMAT(a.admit_time, '%Y-%m-%d %H:%i'), '，出院 ',
                       DATE_FORMAT(a.discharge_time, '%Y-%m-%d %H:%i'), '，出院早于入院 ',
                       TIMESTAMPDIFF(HOUR, a.discharge_time, a.admit_time), ' 小时') AS detail,
                   IFNULL(DATE_FORMAT(a.discharge_time, '%Y-%m-%d %H:%i:%s'), '') AS occurredTime
            FROM biz_admission a
            WHERE a.del_flag = 0 AND a.discharge_time IS NOT NULL AND a.discharge_time < a.admit_time
            ORDER BY a.admit_time DESC
            """)
    List<QualityIssueVO> ruleDateReverse();

    /**
     * L1 记账行允许负数（红冲即负行），非法只剩三种：单价为负、0 量/0 额、数量与金额符号相反
     */
    @Select("""
            SELECT 'AMOUNT-INVALID'        AS ruleCode,
                   'biz_fee_record'        AS tableName,
                   f.id      AS recordId,
                   f.fee_no                AS recordNo,
                   f.patient_id AS patientId,
                   IFNULL(f.patient_no, '')   AS patientNo,
                   IFNULL(f.patient_name, '') AS patientName,
                   IFNULL(f.dept_name, '')    AS deptName,
                   IFNULL(f.doctor_name, '')  AS doctorName,
                   CONCAT('记账行「', IFNULL(f.item_name, ''), '」数量 ', f.quantity, '，单价 ', f.price,
                       '，金额 ', f.amount, '（红冲行数量与金额须同为负；不得 0 量 0 额或符号相反）') AS detail,
                   IFNULL(DATE_FORMAT(f.book_time, '%Y-%m-%d %H:%i:%s'), '') AS occurredTime
            FROM biz_fee_record f
            WHERE f.del_flag = 0 AND (f.price < 0 OR f.quantity = 0 OR f.amount = 0
                  OR SIGN(f.amount) <> SIGN(f.quantity))
            ORDER BY f.book_time DESC
            """)
    List<QualityIssueVO> ruleAmountInvalid();
}

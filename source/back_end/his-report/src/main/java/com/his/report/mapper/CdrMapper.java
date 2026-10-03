package com.his.report.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * CDR 患者全景时间轴 Mapper（P5.2）。
 *
 * <p>为什么是一大坨 UNION ALL 而不是"一个事件源一个方法"：
 * 一次患者全景要拉 28 类事件，逐个方法查就是 28 次数据库往返（还要乘上归并后的档案数）。
 * 统一成"同一形状的行"一次性取回，Java 侧再按锚点分组 —— 这是 CDR 唯一能跑得动的取数方式。
 *
 * <p>三条必须说清的代价（表结构变了这里不会编译报错，只会静默少字段/空值）：
 * <ol>
 *   <li>每个分支只碰**已核过存在**的列，且时间列都有 COALESCE 兜底
 *       （否则 etime 为 null，事件会在时间轴上排到最后，看着像"数据丢了"）；</li>
 *   <li>没有删除标记的表（就诊次 / 治疗申请单）不加该条件，
 *       加了会直接报"Unknown column"，宁可先核一遍；</li>
 *   <li>凡是"经别的表才能找到就诊次"的事件（检验/检查报告、报告单），
 *       关联用**标量子查询**而不是 JOIN —— JOIN 一旦一对多就会把一条事件复制成多条，
 *       而重复事件在时间轴上是看不出来的。</li>
 * </ol>
 *
 * <p>另外：报告单用 NOT EXISTS 排掉已被检验/检查记录表表达过的同号记录，
 * 否则同一份报告会在时间轴上出现两次。
 */
@Mapper
public interface CdrMapper {

    /**
     * 拉取该患者（EMPI 归并后的全部档案）的所有事件。
     *
     * <p>返回行形状（列名即键）：
     * {@code etype, src_table, src_id, anchor_type, anchor_id, etime, title, summary,
     * dept_name, operator_name, status_code, secondary_code, amount, owner_pid}
     */
    String EVENT_SQL = """
            <script>
            SELECT 'outpatientRecord' AS etype, 'biz_medical_record' AS src_table, CAST(m.id AS CHAR) AS src_id,
                   'REGIST' AS anchor_type, CAST(m.regist_id AS CHAR) AS anchor_id,
                   COALESCE(m.create_time, TIMESTAMP(m.visit_date)) AS etime,
                   CONCAT('门诊病历 ', m.record_no) AS title,
                   NULLIF(TRIM(CONCAT(COALESCE(LEFT(m.chief_complaint, 60), ''),
                     CASE WHEN m.diagnosis_name IS NULL OR m.diagnosis_name = '' THEN ''
                          ELSE CONCAT(' / 诊断：', LEFT(m.diagnosis_name, 40)) END)), '') AS summary,
                   m.dept_name AS dept_name, m.doctor_name AS operator_name, m.record_status AS status_code,
                   CAST(NULL AS SIGNED) AS secondary_code, CAST(NULL AS DECIMAL(14,2)) AS amount,
                   CAST(m.patient_id AS CHAR) AS owner_pid
              FROM biz_medical_record m
             WHERE m.del_flag = 0
               AND m.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'prescription', 'biz_prescription', CAST(p.id AS CHAR), 'REGIST', CAST(p.regist_id AS CHAR),
                   COALESCE(p.submit_time, p.create_time, TIMESTAMP(p.visit_date)),
                   CONCAT('处方 ', p.prescription_no),
                   NULLIF(TRIM(CONCAT(COALESCE(p.drug_count, 0), ' 种药品',
                     CASE WHEN p.diagnosis IS NULL OR p.diagnosis = '' THEN ''
                          ELSE CONCAT(' / ', LEFT(p.diagnosis, 40)) END)), ''),
                   p.dept_name, p.doctor_name, p.prescription_status, p.prescription_type, p.total_amount,
                   CAST(p.patient_id AS CHAR)
              FROM biz_prescription p
             WHERE p.del_flag = 0
               AND p.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'laboratoryApply', 'biz_laboratory_apply', CAST(a.id AS CHAR), 'REGIST', CAST(a.regist_id AS CHAR),
                   COALESCE(a.submit_time, a.create_time, TIMESTAMP(a.visit_date)),
                   CONCAT('检验申请 ', a.apply_no),
                   NULLIF(TRIM(CONCAT(COALESCE(a.laboratory_item_name, ''),
                     CASE WHEN a.specimen_type IS NULL OR a.specimen_type = '' THEN ''
                          ELSE CONCAT(' / 标本：', a.specimen_type) END)), ''),
                   a.dept_name, a.doctor_name, a.apply_status, CAST(NULL AS SIGNED), a.price,
                   CAST(a.patient_id AS CHAR)
              FROM biz_laboratory_apply a
             WHERE a.del_flag = 0
               AND a.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'inspectionApply', 'biz_inspection_apply', CAST(a.id AS CHAR), 'REGIST', CAST(a.regist_id AS CHAR),
                   COALESCE(a.submit_time, a.appointment_time, a.create_time, TIMESTAMP(a.visit_date)),
                   CONCAT('检查申请 ', a.apply_no),
                   NULLIF(TRIM(CONCAT(COALESCE(a.inspection_item_name, ''),
                     CASE WHEN a.body_part IS NULL OR a.body_part = '' THEN ''
                          ELSE CONCAT(' / 部位：', a.body_part) END)), ''),
                   a.dept_name, a.doctor_name, a.apply_status, CAST(NULL AS SIGNED), a.price,
                   CAST(a.patient_id AS CHAR)
              FROM biz_inspection_apply a
             WHERE a.del_flag = 0
               AND a.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'treatmentApply', 'biz_treatment_apply', CAST(t.apply_id AS CHAR), 'REGIST', CAST(t.regist_id AS CHAR),
                   COALESCE(t.execute_time, t.apply_time),
                   CONCAT('治疗单 ', t.apply_no),
                   NULLIF(LEFT(t.remark, 60), ''),
                   NULL, NULL, t.apply_status, CAST(NULL AS SIGNED), CAST(NULL AS DECIMAL(14,2)),
                   CAST(t.patient_id AS CHAR)
              FROM biz_treatment_apply t
             WHERE t.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'charge', 'biz_settlement_bill', CAST(s.id AS CHAR), 'REGIST', CAST(s.encounter_id AS CHAR),
                   COALESCE(s.bill_time, s.create_time),
                   CONCAT('收费单 ', s.bill_no),
                   CONCAT('总额 ', COALESCE(s.total_amount, 0), '，实收 ', COALESCE(s.paid_amount, 0)),
                   NULL, s.bill_by_name, s.bill_status, CAST(NULL AS SIGNED), s.total_amount,
                   CAST(s.patient_id AS CHAR)
              FROM biz_settlement_bill s
             WHERE s.del_flag = 0 AND s.encounter_type = 1
               AND s.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'insuranceSettlement', 'biz_insurance_settlement', CAST(s.id AS CHAR), 'REGIST', CAST(s.regist_id AS CHAR),
                   COALESCE(s.upload_time, s.audit_time, s.create_time),
                   CONCAT('医保结算单 ', s.settlement_no),
                   CONCAT('总额 ', COALESCE(s.total_amount, 0), '，医保支付 ', COALESCE(s.insurance_pay, 0),
                          '，个人自付 ', COALESCE(s.personal_pay, 0)),
                   s.dept_name, s.doctor_name, s.settlement_status, CAST(NULL AS SIGNED), s.total_amount,
                   CAST(s.patient_id AS CHAR)
              FROM biz_insurance_settlement s
             WHERE s.del_flag = 0
               AND s.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'queue', 'biz_queue', CAST(q.id AS CHAR), 'REGIST', CAST(q.regist_id AS CHAR),
                   COALESCE(q.call_time, q.start_time, q.arrive_time, q.create_time),
                   CONCAT('候诊叫号 ', q.queue_no),
                   NULLIF(TRIM(CONCAT('序号 ', COALESCE(q.sequence_no, 0),
                     CASE WHEN q.wait_duration IS NULL THEN '' ELSE CONCAT('，等候 ', q.wait_duration, ' 分钟') END)), ''),
                   q.dept_name, q.doctor_name, q.queue_status, CAST(NULL AS SIGNED), CAST(NULL AS DECIMAL(14,2)),
                   CAST(q.patient_id AS CHAR)
              FROM biz_queue q
             WHERE q.del_flag = 0
               AND q.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'recordArchive', 'biz_medical_record_archive', CAST(x.id AS CHAR), 'REGIST', CAST(x.regist_id AS CHAR),
                   COALESCE(x.archive_time, x.create_time),
                   CONCAT('病案归档 ', x.archive_no),
                   NULLIF(LEFT(x.diagnosis, 60), ''),
                   x.dept_name, x.doctor_name, x.archive_status, CAST(NULL AS SIGNED), CAST(NULL AS DECIMAL(14,2)),
                   CAST(x.patient_id AS CHAR)
              FROM biz_medical_record_archive x
             WHERE x.del_flag = 0
               AND x.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'laboratoryReport', 'biz_laboratory_record', CAST(r.id AS CHAR), 'REGIST',
                   CAST((SELECT ap.regist_id FROM biz_laboratory_apply ap
                          WHERE ap.apply_no = r.apply_no AND ap.del_flag = 0 LIMIT 1) AS CHAR),
                   COALESCE(r.report_time, r.audit_time, r.create_time, TIMESTAMP(r.visit_date)),
                   CONCAT('检验报告 ', r.record_no),
                   NULLIF(TRIM(CONCAT(COALESCE(r.laboratory_item_name, ''),
                     CASE WHEN r.diagnosis IS NULL OR r.diagnosis = '' THEN ''
                          ELSE CONCAT(' / ', LEFT(r.diagnosis, 40)) END)), ''),
                   r.apply_dept_name, r.apply_doctor_name, r.record_status, CAST(NULL AS SIGNED), r.price,
                   CAST(r.patient_id AS CHAR)
              FROM biz_laboratory_record r
             WHERE r.del_flag = 0
               AND r.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'inspectionReport', 'biz_inspection_record', CAST(r.id AS CHAR), 'REGIST',
                   CAST((SELECT ap.regist_id FROM biz_inspection_apply ap
                          WHERE ap.apply_no = r.apply_no AND ap.del_flag = 0 LIMIT 1) AS CHAR),
                   COALESCE(r.report_time, r.audit_time, r.create_time, TIMESTAMP(r.visit_date)),
                   CONCAT('检查报告 ', r.record_no),
                   NULLIF(TRIM(CONCAT(COALESCE(r.inspection_item_name, ''),
                     CASE WHEN r.result_conclusion IS NULL OR r.result_conclusion = '' THEN ''
                          ELSE CONCAT(' / ', LEFT(r.result_conclusion, 40)) END)), ''),
                   r.apply_dept_name, r.apply_doctor_name, r.record_status, CAST(NULL AS SIGNED), r.price,
                   CAST(r.patient_id AS CHAR)
              FROM biz_inspection_record r
             WHERE r.del_flag = 0
               AND r.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'report', 'biz_report', CAST(rp.id AS CHAR), 'REGIST',
                   CAST((SELECT m.regist_id FROM biz_medical_record m
                          WHERE m.record_no = rp.record_no AND m.del_flag = 0 LIMIT 1) AS CHAR),
                   COALESCE(rp.publish_time, rp.audit_time, rp.create_time, TIMESTAMP(rp.visit_date)),
                   CONCAT('报告单 ', rp.report_no),
                   NULLIF(LEFT(COALESCE(NULLIF(rp.conclusion, ''), rp.report_content), 60), ''),
                   rp.exam_dept_name, rp.apply_doctor_name, rp.report_status, rp.report_type,
                   CAST(NULL AS DECIMAL(14,2)), CAST(rp.patient_id AS CHAR)
              FROM biz_report rp
             WHERE rp.del_flag = 0
               AND rp.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>
               AND NOT EXISTS (SELECT 1 FROM biz_laboratory_record lr WHERE lr.record_no = rp.record_no)
               AND NOT EXISTS (SELECT 1 FROM biz_inspection_record ir WHERE ir.record_no = rp.record_no)

            UNION ALL
            SELECT 'admission', 'biz_admission', CAST(a.admission_id AS CHAR), 'ADMISSION', CAST(a.admission_id AS CHAR),
                   a.admit_time,
                   CONCAT('入院登记 ', a.admission_no),
                   NULLIF(TRIM(CONCAT(COALESCE(a.admit_diagnosis_name, a.diagnosis, ''),
                     CASE WHEN a.admit_diagnosis_code IS NULL OR a.admit_diagnosis_code = '' THEN ''
                          ELSE CONCAT(' / ICD ', a.admit_diagnosis_code) END)), ''),
                   (SELECT d.dept_name FROM sys_department d WHERE d.id = a.dept_id),
                   NULL, a.admit_status, a.admit_way, CAST(NULL AS DECIMAL(14,2)),
                   CAST(a.patient_id AS CHAR)
              FROM biz_admission a
             WHERE a.del_flag = 0
               AND a.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'inpatientRecord', 'biz_inpatient_record', CAST(r.id AS CHAR), 'ADMISSION', CAST(r.admission_id AS CHAR),
                   COALESCE(r.record_time, r.create_time),
                   CONCAT(COALESCE(r.record_title, '住院文书'), ' ', r.record_no),
                   NULLIF(TRIM(CONCAT(COALESCE(LEFT(r.chief_complaint, 40), ''),
                     CASE WHEN r.diagnosis_name IS NULL OR r.diagnosis_name = '' THEN ''
                          ELSE CONCAT(' / 诊断：', LEFT(r.diagnosis_name, 40)) END)), ''),
                   r.dept_name, r.doctor_name, r.record_status, r.record_type, CAST(NULL AS DECIMAL(14,2)),
                   CAST(r.patient_id AS CHAR)
              FROM biz_inpatient_record r
             WHERE r.del_flag = 0
               AND r.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'inpatientOrder', 'biz_inpatient_order', CAST(o.id AS CHAR), 'ADMISSION', CAST(o.admission_id AS CHAR),
                   COALESCE(o.order_time, o.start_time, o.create_time),
                   COALESCE(o.item_name, '医嘱'),
                   NULLIF(TRIM(CONCAT('数量 ', COALESCE(o.quantity, 0), COALESCE(o.unit, ''),
                     CASE WHEN o.route IS NULL OR o.route = '' THEN '' ELSE CONCAT(' / ', o.route) END,
                     CASE WHEN o.frequency IS NULL OR o.frequency = '' THEN '' ELSE CONCAT(' / ', o.frequency) END)), ''),
                   o.dept_name, o.doctor_name, o.order_status, o.order_type, o.amount,
                   CAST(o.patient_id AS CHAR)
              FROM biz_inpatient_order o
             WHERE o.del_flag = 0
               AND o.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'inpatientDiagnosis', 'biz_inpatient_diagnosis', CAST(g.id AS CHAR), 'ADMISSION', CAST(g.admission_id AS CHAR),
                   g.create_time,
                   CONCAT('诊断 ', COALESCE(g.icd_name, '')),
                   NULLIF(TRIM(CONCAT('ICD ', COALESCE(g.icd_code, ''), ' / 入院病情 ',
                     COALESCE(g.admit_condition, '未填'), CASE WHEN g.cc_level IS NULL OR g.cc_level = '' THEN ''
                          ELSE CONCAT(' / CC-MCC ', g.cc_level) END)), ''),
                   NULL, NULL, CAST(NULL AS SIGNED), g.diag_type,
                   CAST(NULL AS DECIMAL(14,2)),
                   CAST((SELECT a.patient_id FROM biz_admission a WHERE a.admission_id = g.admission_id) AS CHAR)
              FROM biz_inpatient_diagnosis g
             WHERE g.del_flag = 0
               AND g.admission_id IN (SELECT a.admission_id FROM biz_admission a
                                       WHERE a.del_flag = 0
                                         AND a.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>)

            UNION ALL
            SELECT 'inpatientSummary', 'biz_inpatient_summary', CAST(s.id AS CHAR), 'ADMISSION', CAST(s.admission_id AS CHAR),
                   COALESCE(s.discharge_time, s.admit_time, s.create_time),
                   CONCAT('病案首页 ', COALESCE(s.main_diagnosis_name, '未填主诊断')),
                   CONCAT('住院 ', COALESCE(s.inpatient_days, 0), ' 天 / 总费用 ', COALESCE(s.total_amount, 0)),
                   s.dept_name, NULL, s.summary_status, CAST(NULL AS SIGNED), s.total_amount,
                   CAST(s.patient_id AS CHAR)
              FROM biz_inpatient_summary s
             WHERE s.del_flag = 0
               AND s.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'operationApply', 'biz_operation_apply', CAST(o.id AS CHAR), 'ADMISSION', CAST(o.admission_id AS CHAR),
                   COALESCE(o.operation_end_time, o.operation_start_time, o.schedule_time, o.apply_time, o.create_time),
                   CONCAT('手术申请 ', COALESCE(o.actual_operation_name, o.planned_operation_name, o.apply_no)),
                   NULLIF(TRIM(CONCAT(COALESCE(o.apply_dept_name, ''),
                     CASE WHEN o.surgeon_name IS NULL OR o.surgeon_name = '' THEN ''
                          ELSE CONCAT(' / 术者 ', o.surgeon_name) END,
                     CASE WHEN o.anesthesia_type IS NULL OR o.anesthesia_type = '' THEN ''
                          ELSE CONCAT(' / 麻醉 ', o.anesthesia_type) END)), ''),
                   o.apply_dept_name, o.surgeon_name, o.operation_status, CAST(NULL AS SIGNED),
                   CAST(NULL AS DECIMAL(14,2)), CAST(o.patient_id AS CHAR)
              FROM biz_operation_apply o
             WHERE o.del_flag = 0
               AND o.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'inpatientOperation', 'biz_inpatient_operation', CAST(o.id AS CHAR), 'ADMISSION', CAST(o.admission_id AS CHAR),
                   COALESCE(o.operation_date, o.create_time),
                   CONCAT('手术记录 ', COALESCE(o.operation_name, '')),
                   NULLIF(TRIM(CONCAT('术者 ', COALESCE(o.surgeon_name, ''),
                     CASE WHEN o.anesthesia_type IS NULL OR o.anesthesia_type = '' THEN ''
                          ELSE CONCAT(' / 麻醉 ', o.anesthesia_type) END)), ''),
                   NULL, o.surgeon_name, CAST(NULL AS SIGNED), o.is_main,
                   CAST(NULL AS DECIMAL(14,2)),
                   CAST((SELECT a.patient_id FROM biz_admission a WHERE a.admission_id = o.admission_id) AS CHAR)
              FROM biz_inpatient_operation o
             WHERE o.del_flag = 0
               AND o.admission_id IN (SELECT a.admission_id FROM biz_admission a
                                       WHERE a.del_flag = 0
                                         AND a.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>)

            UNION ALL
            SELECT 'consultation', 'biz_consultation', CAST(c.consultation_id AS CHAR), 'ADMISSION', CAST(c.admission_id AS CHAR),
                   COALESCE(c.finish_time, c.consult_time, c.accept_time, c.apply_time, c.create_time),
                   CONCAT('会诊 ', c.consultation_no),
                   NULLIF(TRIM(CONCAT('事由：', COALESCE(LEFT(c.reason, 50), ''), CASE WHEN c.is_urgent = 1 THEN ' / 急会诊' ELSE '' END)), ''),
                   NULL, c.apply_doctor_name, c.consult_status, c.consult_type,
                   CAST(NULL AS DECIMAL(14,2)), CAST(c.patient_id AS CHAR)
              FROM biz_consultation c
             WHERE c.del_flag = 0 AND c.admission_id IS NOT NULL
               AND c.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'transfer', 'biz_inpatient_transfer', CAST(t.id AS CHAR), 'ADMISSION', CAST(t.admission_id AS CHAR),
                   COALESCE(t.receive_time, t.apply_time, t.create_time),
                   CONCAT('转科 ', COALESCE(t.from_dept_name, '?'), ' → ', COALESCE(t.to_dept_name, '?')),
                   NULLIF(TRIM(CONCAT('原因：', COALESCE(LEFT(t.transfer_reason, 50), ''),
                     CASE WHEN t.stop_orders_count IS NULL THEN '' ELSE CONCAT(' / 停长期医嘱 ', t.stop_orders_count, ' 条') END)), ''),
                   t.to_dept_name, t.apply_doctor_name, t.transfer_status, CAST(NULL AS SIGNED),
                   CAST(NULL AS DECIMAL(14,2)), CAST(t.patient_id AS CHAR)
              FROM biz_inpatient_transfer t
             WHERE t.del_flag = 0
               AND t.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'transfusion', 'biz_transfusion_apply', CAST(t.id AS CHAR), 'ADMISSION', CAST(t.admission_id AS CHAR),
                   COALESCE(t.infusion_start_time, t.issue_time, t.apply_time, t.create_time),
                   CONCAT('输血 ', COALESCE(t.blood_component, '成分血')),
                   NULLIF(TRIM(CONCAT(COALESCE(t.patient_abo, ''), COALESCE(t.patient_rh, ''),
                     CASE WHEN t.bag_count IS NULL THEN '' ELSE CONCAT(' / ', t.bag_count, ' 袋') END,
                     CASE WHEN t.transfusion_purpose IS NULL OR t.transfusion_purpose = '' THEN ''
                          ELSE CONCAT(' / ', LEFT(t.transfusion_purpose, 30)) END)), ''),
                   t.apply_dept_name, t.apply_doctor_name, t.transfusion_status, CAST(NULL AS SIGNED), t.planned_amount,
                   CAST(t.patient_id AS CHAR)
              FROM biz_transfusion_apply t
             WHERE t.del_flag = 0
               AND t.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'nursingRecord', 'biz_nursing_record', CAST(n.id AS CHAR), 'ADMISSION', CAST(n.admission_id AS CHAR),
                   COALESCE(n.measure_time, n.create_time),
                   CONCAT('护理记录 ', n.record_no),
                   NULLIF(TRIM(CONCAT('体温 ', COALESCE(n.temperature, '-'), ' 脉搏 ', COALESCE(n.pulse, '-'),
                     ' 呼吸 ', COALESCE(n.respiration, '-'), ' 血压 ', COALESCE(n.systolic_pressure, '-'), '/',
                     COALESCE(n.diastolic_pressure, '-'))), ''),
                   n.dept_name, n.nurse_name, n.record_status, n.nursing_type, CAST(NULL AS DECIMAL(14,2)),
                   CAST(n.patient_id AS CHAR)
              FROM biz_nursing_record n
             WHERE n.del_flag = 0
               AND n.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'prepay', 'biz_payment_txn', CAST(p.id AS CHAR), 'ADMISSION', CAST(p.encounter_id AS CHAR),
                   COALESCE(p.txn_time, p.create_time),
                   CONCAT('预交金 ', p.txn_no),
                   CONCAT('金额 ', COALESCE(p.amount, 0),
                          CASE WHEN p.receipt_no IS NULL OR p.receipt_no = '' THEN ''
                               ELSE CONCAT(' / 收据 ', p.receipt_no) END),
                   NULL, p.cashier_name, CAST(NULL AS SIGNED), p.pay_method, p.amount,
                   CAST(p.patient_id AS CHAR)
              FROM biz_payment_txn p
             WHERE p.del_flag = 0 AND p.bill_id IS NULL AND p.source_type = 3 AND p.encounter_type = 2
               AND p.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'discharge', 'biz_discharge', CAST(d.discharge_id AS CHAR), 'ADMISSION', CAST(d.admission_id AS CHAR),
                   COALESCE(d.discharge_time, d.create_time),
                   CONCAT('出院 ', d.discharge_no),
                   NULLIF(TRIM(CONCAT('出院诊断 ', COALESCE(LEFT(d.discharge_diagnosis, 50), ''),
                     CASE WHEN d.death_flag = 1 THEN ' / 死亡' ELSE '' END)), ''),
                   NULL, NULL, d.discharge_status, CAST(NULL AS SIGNED),
                   CAST(NULL AS DECIMAL(14,2)), CAST(d.patient_id AS CHAR)
              FROM biz_discharge d
             WHERE d.del_flag = 0
               AND d.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'inpatientSettlement', 'biz_settlement_bill', CAST(s.id AS CHAR), 'ADMISSION', CAST(s.encounter_id AS CHAR),
                   COALESCE(s.bill_time, s.create_time),
                   CONCAT('住院结算 ', s.bill_no),
                   CONCAT('总额 ', COALESCE(s.total_amount, 0), ' / 统筹 ', COALESCE(s.pool_amount, 0),
                          ' / 应缴 ', COALESCE(s.payable_amount, 0),
                          ' / 欠费 ', COALESCE(GREATEST(s.payable_amount - s.paid_amount, 0), 0)),
                   NULL, s.bill_by_name, s.bill_status, CAST(NULL AS SIGNED), s.total_amount,
                   CAST(s.patient_id AS CHAR)
              FROM biz_settlement_bill s
             WHERE s.del_flag = 0 AND s.encounter_type = 2 AND s.bill_type = 4
               AND s.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'charge', 'biz_settlement_bill', CAST(s.id AS CHAR), 'ADMISSION', CAST(s.encounter_id AS CHAR),
                   COALESCE(s.bill_time, s.create_time),
                   CONCAT('住院收费单 ', s.bill_no),
                   CONCAT('总额 ', COALESCE(s.total_amount, 0), '，实收 ', COALESCE(s.paid_amount, 0)),
                   NULL, s.bill_by_name, s.bill_status, CAST(NULL AS SIGNED), s.total_amount,
                   CAST(s.patient_id AS CHAR)
              FROM biz_settlement_bill s
             WHERE s.del_flag = 0 AND s.encounter_type = 2 AND s.bill_type != 4
               AND s.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'emergency', 'biz_emergency', CAST(e.id AS CHAR), 'EMERGENCY', CAST(e.id AS CHAR),
                   COALESCE(e.diagnosis_time, e.admission_time, e.create_time),
                   CONCAT('急诊 ', e.emergency_no),
                   NULLIF(TRIM(CONCAT(COALESCE(LEFT(e.chief_complaint, 50), ''),
                     CASE WHEN e.diagnosis IS NULL OR e.diagnosis = '' THEN ''
                          ELSE CONCAT(' / 初步诊断：', LEFT(e.diagnosis, 40)) END)), ''),
                   e.dept_name, e.doctor_name, e.emergency_status, e.triage_level,
                   CAST(NULL AS DECIMAL(14,2)), CAST(e.patient_id AS CHAR)
              FROM biz_emergency e
             WHERE e.del_flag = 0
               AND e.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'criticalValue', 'biz_critical_value', CAST(v.id AS CHAR), 'PATIENT', CAST(v.patient_id AS CHAR),
                   COALESCE(v.report_time, v.create_time),
                   CONCAT('危急值 ', COALESCE(v.item_name, '')),
                   NULLIF(TRIM(CONCAT('结果 ', COALESCE(v.result_value, ''), COALESCE(v.result_unit, ''),
                     CASE WHEN v.reference_range IS NULL OR v.reference_range = '' THEN ''
                          ELSE CONCAT('（参考 ', v.reference_range, '）') END)), ''),
                   v.report_dept_name, v.report_by, v.status, v.critical_type, CAST(NULL AS DECIMAL(14,2)),
                   CAST(v.patient_id AS CHAR)
              FROM biz_critical_value v
             WHERE v.del_flag = 0
               AND v.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'qualityControl', 'biz_quality_control', CAST(q.id AS CHAR), 'PATIENT', CAST(q.patient_id AS CHAR),
                   COALESCE(q.qc_time, q.create_time),
                   CONCAT('病案质控 ', q.qc_no),
                   NULLIF(TRIM(CONCAT('错误 ', COALESCE(q.error_count, 0), ' 处',
                     CASE WHEN q.error_detail IS NULL OR q.error_detail = '' THEN ''
                          ELSE CONCAT('：', LEFT(q.error_detail, 60)) END)), ''),
                   NULL, q.qc_by, q.qc_status, CAST(NULL AS SIGNED), CAST(NULL AS DECIMAL(14,2)),
                   CAST(q.patient_id AS CHAR)
              FROM biz_quality_control q
             WHERE q.del_flag = 0
               AND q.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'followupTask', 'biz_followup_task', CAST(f.id AS CHAR), 'PATIENT', CAST(f.patient_id AS CHAR),
                   COALESCE(f.followup_time, f.execute_time, f.create_time),
                   CONCAT('随访 ', f.task_no),
                   NULLIF(LEFT(COALESCE(f.followup_content, f.diagnosis), 60), ''),
                   NULL, f.executor_name, f.followup_status, CAST(NULL AS SIGNED),
                   CAST(NULL AS DECIMAL(14,2)), CAST(f.patient_id AS CHAR)
              FROM biz_followup_task f
             WHERE f.del_flag = 0
               AND f.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'referral', 'biz_referral', CAST(r.referral_id AS CHAR), 'PATIENT', CAST(r.patient_id AS CHAR),
                   r.referral_time,
                   CONCAT('转诊 ', r.referral_no),
                   NULLIF(LEFT(r.reason, 60), ''),
                   NULL, NULL, r.referral_status, CAST(NULL AS SIGNED),
                   CAST(NULL AS DECIMAL(14,2)), CAST(r.patient_id AS CHAR)
              FROM biz_referral r
             WHERE r.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'publicHealthReport', 'biz_public_health_report', CAST(p.id AS CHAR), 'PATIENT', CAST(p.patient_id AS CHAR),
                   COALESCE(p.report_time, p.create_time),
                   CONCAT('公卫上报 ', p.report_no),
                   NULLIF(TRIM(CONCAT(COALESCE(p.diagnosis, ''), CASE WHEN p.diagnosis_code IS NULL OR p.diagnosis_code = ''
                          THEN '' ELSE CONCAT(' / ICD ', p.diagnosis_code) END)), ''),
                   NULL, p.report_by, p.report_status, p.report_type, CAST(NULL AS DECIMAL(14,2)),
                   CAST(p.patient_id AS CHAR)
              FROM biz_public_health_report p
             WHERE p.del_flag = 0
               AND p.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>
            </script>
            """;

    @Select(EVENT_SQL)
    List<Map<String, Object>> selectTimelineEvents(@Param("ids") Collection<Long> ids);

    /** 住院节点（含科室/病区/床位名，入院记录本身只存 ID） */
    @Select("""
            <script>
            SELECT CAST(a.admission_id AS CHAR) AS admission_id, a.admission_no, CAST(a.patient_id AS CHAR) AS owner_pid,
                   a.admit_time, a.discharge_time, a.admit_status, a.admit_way,
                   COALESCE(a.admit_diagnosis_name, a.diagnosis) AS diagnosis,
                   (SELECT d.dept_name FROM sys_department d WHERE d.id = a.dept_id) AS dept_name,
                   (SELECT w.ward_name FROM sys_ward w WHERE w.ward_id = a.ward_id) AS ward_name,
                   (SELECT b.bed_no FROM sys_bed b WHERE b.bed_id = a.bed_id) AS bed_no,
                   (SELECT s.main_diagnosis_name FROM biz_inpatient_summary s
                     WHERE s.admission_id = a.admission_id AND s.del_flag = 0 LIMIT 1) AS summary_diag,
                   (SELECT s.summary_status FROM biz_inpatient_summary s
                     WHERE s.admission_id = a.admission_id AND s.del_flag = 0 LIMIT 1) AS summary_status
              FROM biz_admission a
             WHERE a.del_flag = 0
               AND a.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>
            </script>
            """)
    List<Map<String, Object>> selectAdmissions(@Param("ids") Collection<Long> ids);

    /** 门诊就诊次（就诊次记录；其挂号ID清单字段存逗号分隔的多个挂号ID） */
    @Select("""
            <script>
            SELECT CAST(v.visit_id AS CHAR) AS visit_id, v.visit_no, CAST(v.patient_id AS CHAR) AS owner_pid,
                   v.start_time, v.end_time, v.visit_status, v.total_amount, v.regist_ids
              FROM biz_visit v
             WHERE v.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>
            </script>
            """)
    List<Map<String, Object>> selectVisits(@Param("ids") Collection<Long> ids);

    /** 挂号（门诊节点的锚点，也是"没有被任何就诊次收录的挂号"的兜底来源） */
    @Select("""
            <script>
            SELECT CAST(r.id AS CHAR) AS regist_id, r.regist_no, CAST(r.patient_id AS CHAR) AS owner_pid,
                   r.regist_time, r.visit_date, r.regist_status, r.dept_name, r.doctor_name,
                   r.regist_type, r.medical_insurance_type
              FROM biz_appoint_info r
             WHERE r.del_flag = 0
               AND r.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>
            </script>
            """)
    List<Map<String, Object>> selectRegistrations(@Param("ids") Collection<Long> ids);

    /** 急诊节点 */
    @Select("""
            <script>
            SELECT CAST(e.id AS CHAR) AS emergency_id, e.emergency_no, CAST(e.patient_id AS CHAR) AS owner_pid,
                   e.admission_time, e.finish_time, e.emergency_status, e.triage_level, e.zone,
                   e.dept_name, e.doctor_name, e.diagnosis, e.chief_complaint
              FROM biz_emergency e
             WHERE e.del_flag = 0
               AND e.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>
            </script>
            """)
    List<Map<String, Object>> selectEmergencies(@Param("ids") Collection<Long> ids);

    /**
     * 健康档案（患者级，不挂就诊次）。
     *
     * <p>返回行：{@code pkey, sid, owner_pid, title, summary, tm}
     */
    String PROFILE_SQL = """
            <script>
            SELECT 'allergy' AS pkey, CAST(x.id AS CHAR) AS sid, CAST(x.patient_id AS CHAR) AS owner_pid,
                   COALESCE(x.allergen_name, x.allergy_type, '过敏原未填') AS title,
                   NULLIF(TRIM(CONCAT(COALESCE(x.allergy_severity, ''),
                     CASE WHEN x.allergy_symptoms IS NULL OR x.allergy_symptoms = '' THEN ''
                          ELSE CONCAT(' / ', LEFT(x.allergy_symptoms, 40)) END)), '') AS summary,
                   x.allergy_date AS tm
              FROM biz_patient_allergy x
             WHERE x.del_flag = 0
               AND x.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'pastDisease', CAST(x.id AS CHAR), CAST(x.patient_id AS CHAR), x.disease_name,
                   NULLIF(TRIM(CONCAT(COALESCE(x.current_status, ''),
                     CASE WHEN x.treatment_plan IS NULL OR x.treatment_plan = '' THEN ''
                          ELSE CONCAT(' / ', LEFT(x.treatment_plan, 40)) END)), ''),
                   x.diagnosis_date
              FROM biz_patient_past_disease x
             WHERE x.del_flag = 0
               AND x.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'surgery', CAST(x.id AS CHAR), CAST(x.patient_id AS CHAR), x.surgery_name,
                   NULLIF(TRIM(CONCAT(COALESCE(x.hospital_name, ''),
                     CASE WHEN x.postop_diagnosis IS NULL OR x.postop_diagnosis = '' THEN ''
                          ELSE CONCAT(' / 术后诊断：', LEFT(x.postop_diagnosis, 40)) END)), ''),
                   x.surgery_date
              FROM biz_patient_surgery_history x
             WHERE x.del_flag = 0
               AND x.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'family', CAST(x.id AS CHAR), CAST(x.patient_id AS CHAR),
                   CONCAT(COALESCE(x.relationship, '亲属'), ' ', COALESCE(x.name, '')),
                   NULLIF(TRIM(CONCAT(COALESCE(x.health_status, ''),
                     CASE WHEN x.hereditary_disease IS NULL OR x.hereditary_disease = '' THEN ''
                          ELSE CONCAT(' / 遗传病：', LEFT(x.hereditary_disease, 30)) END)), ''),
                   CAST(NULL AS DATE)
              FROM biz_patient_family_history x
             WHERE x.del_flag = 0
               AND x.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'medication', CAST(x.id AS CHAR), CAST(x.patient_id AS CHAR), x.drug_name,
                   NULLIF(TRIM(CONCAT(COALESCE(x.dosage, ''), ' ', COALESCE(x.frequency, ''), ' ',
                     COALESCE(x.route, ''))), ''),
                   x.start_date
              FROM biz_patient_medication_history x
             WHERE x.del_flag = 0
               AND x.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>

            UNION ALL
            SELECT 'contact', CAST(x.id AS CHAR), CAST(x.patient_id AS CHAR),
                   CONCAT(COALESCE(x.contact_name, ''), ' ', COALESCE(x.relationship, '')),
                   NULLIF(TRIM(CONCAT(COALESCE(x.phone, ''),
                     CASE WHEN x.address IS NULL OR x.address = '' THEN '' ELSE CONCAT(' / ', LEFT(x.address, 30)) END)), ''),
                   CAST(NULL AS DATE)
              FROM biz_patient_contact x
             WHERE x.del_flag = 0
               AND x.patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>
            </script>
            """;

    @Select(PROFILE_SQL)
    List<Map<String, Object>> selectHealthProfile(@Param("ids") Collection<Long> ids);

    /** 归并进来的档案身份（用于把"数据挂在别的档案号下"这件事说清楚） */
    @Select("""
            <script>
            SELECT CAST(p.id AS CHAR) AS pid, p.patient_no, p.patient_name, p.merge_time, p.merge_status
              FROM biz_patient p
             WHERE p.del_flag = 0
               AND p.id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>
            </script>
            """)
    List<Map<String, Object>> selectArchiveIdentities(@Param("ids") Collection<Long> ids);
}

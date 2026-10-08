package com.his.medicaltech.mapper;

import com.his.medicaltech.vo.WorkbenchDeptVisitRankRowVO;
import com.his.medicaltech.vo.WorkbenchDoctorStatsRowVO;
import com.his.medicaltech.vo.WorkbenchHospitalAlertRowVO;
import com.his.medicaltech.vo.WorkbenchHospitalCoreRowVO;
import com.his.medicaltech.vo.WorkbenchNurseStatsRowVO;
import com.his.medicaltech.vo.WorkbenchWeekTrendRowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 工作台卡片取数 Mapper（一期 A 复用旧 DashboardMapper 的六段 SQL）。
 */
@Mapper
public interface WorkbenchMetricMapper {

    /**
     * 全院今日概况（今日挂号/今日收入/在院/床位）
     *
     * <p>今日实收净额 = L3 今日流水净额（收正退负一起 SUM）：按收费单状态反推会把
     * "昨天收、今天退"的那笔漏掉，收银员点钞数就对不上。
     */
    @Select("""
            SELECT
              (SELECT COUNT(*) FROM biz_appoint_info r
                WHERE r.del_flag = 0 AND r.visit_date = CURDATE()) AS todayRegistCount,
              (SELECT IFNULL(SUM(t.amount), 0) FROM biz_payment_txn t
                WHERE t.del_flag = 0 AND t.txn_status = 1 AND DATE(t.txn_time) = CURDATE()) AS todayRevenue,
              (SELECT COUNT(*) FROM biz_admission a
                WHERE a.del_flag = 0 AND a.admit_status = 1) AS inHospitalCount,
              (SELECT COUNT(*) FROM sys_bed b WHERE b.del_flag = 0) AS bedTotal,
              (SELECT COUNT(*) FROM sys_bed b WHERE b.del_flag = 0 AND b.bed_status = 2) AS bedOccupied
            """)
    WorkbenchHospitalCoreRowVO hospitalCoreStats();

    /**
     * 全院异常告警（待处理危急值/待审处方/质控不通过/欠费住院）
     */
    @Select("""
            SELECT
              (SELECT COUNT(*) FROM biz_critical_value v
                WHERE v.del_flag = 0 AND v.status IN (1, 2)) AS criticalValuePending,
              (SELECT COUNT(*) FROM biz_prescription p
                WHERE p.del_flag = 0 AND p.prescription_status = 2) AS prescriptionPending,
              (SELECT COUNT(*) FROM biz_quality_control q
                WHERE q.del_flag = 0 AND q.qc_result = 0) AS qcFailCount,
              (SELECT COUNT(*) FROM (
                  SELECT a.admission_id,
                         IFNULL((SELECT SUM(r.amount) FROM biz_fee_record r
                                  WHERE r.del_flag = 0 AND r.fee_status <> 4
                                    AND r.encounter_type = 2 AND r.encounter_id = a.admission_id), 0) AS charged,
                         IFNULL((SELECT SUM(t.amount) FROM biz_payment_txn t
                                  WHERE t.del_flag = 0 AND t.txn_status = 1
                                    AND t.encounter_type = 2 AND t.encounter_id = a.admission_id
                                    AND ((t.bill_id IS NULL AND t.source_type = 3)
                                         OR (t.bill_id IS NOT NULL AND t.pay_method <> 5))), 0) AS collected
                    FROM biz_admission a
                   WHERE a.del_flag = 0 AND a.admit_status = 1
              ) t WHERE t.charged > t.collected) AS arrearsCount
            """)
    WorkbenchHospitalAlertRowVO hospitalAlertStats();

    /**
     * 近 7 天挂号趋势（按 visit_date 分组，缺日不补零，补零在 Provider 侧）
     */
    @Select("""
            SELECT DATE_FORMAT(visit_date, '%Y-%m-%d') AS date, COUNT(*) AS cnt
              FROM biz_appoint_info
             WHERE del_flag = 0
               AND visit_date >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
               AND visit_date <= CURDATE()
             GROUP BY visit_date
             ORDER BY visit_date
            """)
    List<WorkbenchWeekTrendRowVO> weekRegistTrend();

    /**
     * 今日科室就诊排行（top 6）
     */
    @Select("""
            SELECT COALESCE(NULLIF(dept_name, ''), '未知科室') AS deptName, COUNT(*) AS cnt
              FROM biz_appoint_info
             WHERE del_flag = 0 AND visit_date = CURDATE()
             GROUP BY dept_id, dept_name
             ORDER BY cnt DESC
             LIMIT 6
            """)
    List<WorkbenchDeptVisitRankRowVO> deptVisitRank();

    /**
     * 医生今日诊疗（排班/候诊/住院/待办），按 employeeId 收敛
     */
    @Select("""
            SELECT
              (SELECT COUNT(*) FROM biz_schedule s
                WHERE s.del_flag = 0 AND s.doctor_id = #{doctorId}
                  -- 医生工作台的「今日排班」只算出诊班：sql/195 起排班表承载全院岗位
                  -- （护士/技师/收费员的出勤排班也在这一张表），不加这道条件会把出勤班算成出诊班
                  AND s.staff_type = 1
                  AND s.schedule_date = CURDATE() AND s.status IN (1, 2)) AS todayScheduleCount,
              (SELECT COUNT(*) FROM biz_queue q
                WHERE q.del_flag = 0 AND q.doctor_id = #{doctorId}
                  AND q.queue_status IN (1, 2, 3)) AS waitingCount,
              (SELECT COUNT(*) FROM biz_admission a
                WHERE a.del_flag = 0 AND a.admit_status = 1
                  AND a.admit_doctor_id = #{doctorId}) AS myInpatientCount,
              (SELECT COUNT(*) FROM biz_inpatient_order o
                WHERE o.del_flag = 0 AND o.doctor_id = #{doctorId}
                  AND o.order_status = 1) AS todoVerifyOrderCount,
              (SELECT COUNT(*) FROM biz_consultation c
                WHERE c.del_flag = 0 AND c.consult_status IN (0, 3)
                  AND (c.accept_doctor_id = #{doctorId} OR c.doctor_id = #{doctorId} OR c.to_dept_id = #{deptId})) AS todoConsultationCount,
              (SELECT COUNT(*) FROM biz_critical_value v
                 JOIN biz_laboratory_record lr ON lr.id = v.record_id AND lr.del_flag = 0
                WHERE v.del_flag = 0 AND v.status IN (1, 2)
                  AND lr.apply_doctor_id = #{doctorId}) AS criticalValueCount,
              (SELECT COUNT(*) FROM biz_medical_record_archive x
                WHERE x.del_flag = 0 AND x.archive_status = 1
                  AND x.doctor_id = #{doctorId}) AS todoArchiveCount
            """)
    WorkbenchDoctorStatsRowVO doctorStats(@Param("doctorId") Long doctorId, @Param("deptId") Long deptId);

    /**
     * 病区今日概况（在院/床位/今日入出/待执行），病区=当前 deptId 推导。
     *
     * <p>子查询为空时 IN (空集) 匹配 0 行，计数自然为 0，无需先取病区列表。
     */
    @Select("""
            SELECT
              (SELECT COUNT(*) FROM biz_admission a
                WHERE a.del_flag = 0 AND a.admit_status = 1
                  AND a.ward_id IN (SELECT w.ward_id FROM sys_ward w WHERE w.dept_id = #{deptId})) AS wardInpatientCount,
              (SELECT COUNT(*) FROM sys_bed b
                WHERE b.del_flag = 0
                  AND b.ward_id IN (SELECT w.ward_id FROM sys_ward w WHERE w.dept_id = #{deptId})) AS bedTotal,
              (SELECT COUNT(*) FROM sys_bed b
                WHERE b.del_flag = 0 AND b.bed_status = 2
                  AND b.ward_id IN (SELECT w.ward_id FROM sys_ward w WHERE w.dept_id = #{deptId})) AS bedOccupied,
              (SELECT COUNT(*) FROM biz_admission a
                WHERE a.del_flag = 0 AND a.admit_status = 1 AND DATE(a.admit_time) = CURDATE()
                  AND a.ward_id IN (SELECT w.ward_id FROM sys_ward w WHERE w.dept_id = #{deptId})) AS todayAdmitCount,
              (SELECT COUNT(*) FROM biz_discharge d
                 JOIN biz_admission a ON a.admission_id = d.admission_id AND a.del_flag = 0
                WHERE d.del_flag = 0 AND DATE(d.discharge_time) = CURDATE()
                  AND a.ward_id IN (SELECT w.ward_id FROM sys_ward w WHERE w.dept_id = #{deptId})) AS todayDischargeCount,
              (SELECT COUNT(*) FROM biz_inpatient_order_exec e
                 JOIN biz_inpatient_order o ON o.id = e.order_id AND o.del_flag = 0
                WHERE e.del_flag = 0 AND e.exec_status = 1
                  AND o.ward_id IN (SELECT w.ward_id FROM sys_ward w WHERE w.dept_id = #{deptId})) AS todoExecCount
            """)
    WorkbenchNurseStatsRowVO nurseStats(@Param("deptId") Long deptId);
}

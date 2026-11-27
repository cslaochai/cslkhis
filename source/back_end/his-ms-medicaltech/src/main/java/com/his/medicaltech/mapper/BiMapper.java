package com.his.medicaltech.mapper;

import com.his.medicaltech.vo.BiBedStatRowVO;
import com.his.medicaltech.vo.BiCodedSummaryRowVO;
import com.his.medicaltech.vo.BiDayAmountRowVO;
import com.his.medicaltech.vo.BiDayCountRowVO;
import com.his.medicaltech.vo.BiDeptAmountRowVO;
import com.his.medicaltech.vo.BiDischargeWindowRowVO;
import com.his.medicaltech.vo.BiDrgWeightRowVO;
import com.his.medicaltech.vo.BiOverviewVO;
import com.his.medicaltech.vo.BiWindowRevenueRowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * BI 驾驶舱聚合 Mapper（跨模块只读，全部裸 SQL，一处收口）。
 */
@Mapper
public interface BiMapper {

    /**
     * 收入净额 = L1 记账行带符号合计（红冲写负行、金额一经写入不再改，所以净额只能 SUM 现算），
     * 账务归属时点用 book_time（记账时间），不用 create_time。
     */
    String REVENUE_EXPR = "SUM(f.amount)";

    @Select("SELECT COUNT(*) FROM biz_appoint_info WHERE del_flag = 0 AND visit_date = CURDATE()")
    Long countTodayAppointments();

    @Select("SELECT COUNT(*) FROM biz_admission WHERE del_flag = 0 AND discharge_time IS NULL")
    Long countInHospital();

    @Select("SELECT COUNT(*) FROM biz_admission WHERE del_flag = 0 AND discharge_time IS NOT NULL AND DATE(discharge_time) = CURDATE()")
    Long countTodayDischarge();

    @Select("SELECT COUNT(*) FROM biz_admission WHERE del_flag = 0 AND DATE(admit_time) = CURDATE()")
    Long countTodayAdmissions();

    @Select("SELECT COUNT(*) FROM biz_admission WHERE del_flag = 0 AND DATE(admit_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
    Long countYesterdayAdmissions();

    @Select("SELECT COUNT(*) FROM biz_admission WHERE del_flag = 0 AND discharge_time IS NOT NULL "
            + "AND DATE(discharge_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
    Long countYesterdayDischarge();

    /** 今日主手术台次：一次住院的主手术（is_main=1）算一台，辅助操作不计。 */
    @Select("SELECT COUNT(*) FROM biz_inpatient_operation WHERE del_flag = 0 AND is_main = 1 "
            + "AND DATE(operation_date) = CURDATE()")
    Long countTodayOperations();

    @Select("SELECT COUNT(*) FROM biz_inpatient_operation WHERE del_flag = 0 AND is_main = 1 "
            + "AND DATE(operation_date) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
    Long countYesterdayOperations();

    /** 待入院排队：待收治住院证（order_status=1），已收治/作废/过期不计。 */
    @Select("SELECT COUNT(*) FROM biz_admission_order WHERE del_flag = 0 AND order_status = 1")
    Long countPendingAdmission();

    @Select("SELECT COUNT(*) FROM biz_appoint_info WHERE del_flag = 0 "
            + "AND visit_date = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
    Long countYesterdayAppointments();

    @Select("SELECT IFNULL(ROUND(" + REVENUE_EXPR + ",2),0) FROM biz_fee_record f "
            + "WHERE f.del_flag = 0 AND DATE(f.book_time) = CURDATE()")
    BigDecimal sumTodayRevenue();

    @Select("SELECT IFNULL(ROUND(" + REVENUE_EXPR + ",2),0) FROM biz_fee_record f "
            + "WHERE f.del_flag = 0 AND f.item_type IN (2,3,4) AND DATE(f.book_time) = CURDATE()")
    BigDecimal sumTodayDrugRevenue();

    @Select("SELECT IFNULL(ROUND(" + REVENUE_EXPR + ",2),0) FROM biz_fee_record f "
            + "WHERE f.del_flag = 0 AND DATE(f.book_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
    BigDecimal sumYesterdayRevenue();

    @Select("SELECT IFNULL(ROUND(" + REVENUE_EXPR + ",2),0) FROM biz_fee_record f "
            + "WHERE f.del_flag = 0 AND f.item_type IN (2,3,4) "
            + "AND DATE(f.book_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
    BigDecimal sumYesterdayDrugRevenue();

    @Select("SELECT IFNULL(SUM(CASE WHEN bed_status = 0 THEN 1 ELSE 0 END),0) repair, "
            + "IFNULL(SUM(CASE WHEN bed_status = 2 THEN 1 ELSE 0 END),0) occupied, COUNT(*) total "
            + "FROM sys_bed WHERE del_flag = 0")
    BiBedStatRowVO bedStat();

    @Select("SELECT DATE_FORMAT(visit_date, '%m-%d') statDate, COUNT(*) cnt FROM biz_appoint_info "
            + "WHERE del_flag = 0 AND visit_date >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) "
            + "GROUP BY statDate ORDER BY statDate")
    List<BiDayCountRowVO> appointmentTrend();

    @Select("SELECT DATE_FORMAT(f.book_time, '%m-%d') statDate, ROUND(" + REVENUE_EXPR + ",2) amount FROM biz_fee_record f "
            + "WHERE f.del_flag = 0 AND f.book_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) "
            + "GROUP BY statDate ORDER BY statDate")
    List<BiDayAmountRowVO> revenueTrend();

    @Select("SELECT IFNULL(f.dept_name, '未分配科室') deptName, ROUND(" + REVENUE_EXPR + ",2) amount FROM biz_fee_record f "
            + "WHERE f.del_flag = 0 AND f.book_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) "
            + "GROUP BY f.dept_name ORDER BY amount DESC LIMIT 5")
    List<BiDeptAmountRowVO> deptTop();

    /**
     * 近 30 日收入结构：药品归并西药/中成药/中药饮片，其余按 his_charge_item_type 大类归口；
     * 净额口径与今日收入同源（红冲负行现算）。
     */
    @Select("SELECT code, MAX(name) AS name, ROUND(SUM(amount), 2) AS amount FROM ("
            + "SELECT CASE WHEN f.item_type IN (2,3,4) THEN 'drug' "
            + "WHEN f.item_type = 1 THEN 'registration' WHEN f.item_type = 5 THEN 'exam' "
            + "WHEN f.item_type = 6 THEN 'lab' WHEN f.item_type = 7 THEN 'treatment' "
            + "WHEN f.item_type = 8 THEN 'consumable' ELSE 'other' END AS code, "
            + "CASE WHEN f.item_type IN (2,3,4) THEN '药品' WHEN f.item_type = 1 THEN '诊查费' "
            + "WHEN f.item_type = 5 THEN '检查' WHEN f.item_type = 6 THEN '检验' "
            + "WHEN f.item_type = 7 THEN '治疗' WHEN f.item_type = 8 THEN '耗材材料' ELSE '其他' END AS name, "
            + REVENUE_EXPR + " AS amount "
            + "FROM biz_fee_record f WHERE f.del_flag = 0 "
            + "AND f.book_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) "
            + "GROUP BY f.item_type"
            + ") t GROUP BY code ORDER BY amount DESC")
    List<BiOverviewVO.RevenueStructure> revenueStructure30d();

    // 国考四指标（M5，统一「近 30 日」窗口）

    /**
     * 近 30 日出院队列：出院人数 + 占用总床日。
     * 口径：床日 = TIMESTAMPDIFF(DAY, admit_time, discharge_time)，当天入当天出按 1 计（GREATEST 兜底）；
     * discharge_time 回填缺失（admit_status 已置出院但时间空）的脏行不计入，避免床日算成 NULL。
     */
    @Select("SELECT COUNT(*) dischargeCount, "
            + "IFNULL(SUM(GREATEST(TIMESTAMPDIFF(DAY, admit_time, discharge_time), 1)), 0) bedDays "
            + "FROM biz_admission WHERE del_flag = 0 AND discharge_time IS NOT NULL "
            + "AND discharge_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)")
    BiDischargeWindowRowVO dischargeWindow30d();

    /**
     * 近 30 日收入净额（记账净额，与今日口径同源）
     */
    @Select("SELECT IFNULL(ROUND(" + REVENUE_EXPR + ",2),0) FROM biz_fee_record f "
            + "WHERE f.del_flag = 0 AND f.book_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)")
    BigDecimal sumWindow30dRevenue();

    /**
     * 近 30 日耗材材料收入净额（item_type=8 耗材材料，字典 his_charge_item_type）
     */
    @Select("SELECT IFNULL(ROUND(" + REVENUE_EXPR + ",2),0) FROM biz_fee_record f "
            + "WHERE f.del_flag = 0 AND f.item_type = 8 AND f.book_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)")
    BigDecimal sumWindow30dMaterialRevenue();

    /** 近 30 日门诊/住院收入拆分（encounter_type 1-门诊 2-住院）。 */
    @Select("SELECT IFNULL(ROUND(SUM(CASE WHEN f.encounter_type = 1 THEN f.amount ELSE 0 END),2),0) outpatient, "
            + "IFNULL(ROUND(SUM(CASE WHEN f.encounter_type = 2 THEN f.amount ELSE 0 END),2),0) inpatient "
            + "FROM biz_fee_record f WHERE f.del_flag = 0 "
            + "AND f.book_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)")
    BiWindowRevenueRowVO windowRevenue30d();

    /** 近 30 日门诊人次（次均分母，口径与今日挂号人次同源，不排退号）。 */
    @Select("SELECT COUNT(*) FROM biz_appoint_info WHERE del_flag = 0 "
            + "AND visit_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)")
    Long countOutpatientVisits30d();

    /**
     * 近 30 日出院且主诊断已编码的病案首页（CMI 分母样本），连分组维度一次带回。
     *
     * <p>手术与其他诊断用聚合子查询拼成逗号串带回，不给每条首页再单查一次明细。
     * 手术编码不能省：内科组的规则形如「主诊断在某集合 且 主手术不在手术集合」，
     * 手术清单为空时这条天然成立，手术病例会被错分到同诊断的内科低权重组，且零报错。
     */
    @Select("SELECT s.main_diagnosis_code icdCode, s.gender, s.age, s.age_unit ageUnit, "
            + "s.birth_weight admissionWeightG, "
            + "(SELECT GROUP_CONCAT(o.operation_code) FROM biz_inpatient_operation o "
            + "  WHERE o.del_flag = 0 AND o.is_main = 1 AND o.admission_id = s.admission_id) mainOperCodes, "
            + "(SELECT GROUP_CONCAT(o.operation_code) FROM biz_inpatient_operation o "
            + "  WHERE o.del_flag = 0 AND o.is_main = 0 AND o.admission_id = s.admission_id) otherOperCodes, "
            + "(SELECT GROUP_CONCAT(d.icd_code) FROM biz_inpatient_diagnosis d "
            + "  WHERE d.del_flag = 0 AND d.diag_type = 2 AND d.admission_id = s.admission_id) otherDiagCodes "
            + "FROM biz_inpatient_summary s "
            + "JOIN biz_admission a ON a.admission_id = s.admission_id AND a.del_flag = 0 "
            + "WHERE s.del_flag = 0 AND s.main_diagnosis_code IS NOT NULL AND s.main_diagnosis_code != '' "
            + "AND a.discharge_time IS NOT NULL AND a.discharge_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)")
    List<BiCodedSummaryRowVO> codedSummaries30d();

    /**
     * DRG 细分组权重（官方方案包不含权重，由统筹区医保局下发；为空的组不参与 CMI 计算）
     */
    @Select("SELECT drg_code AS drgCode, weight FROM biz_drg_group WHERE del_flag = 0")
    List<BiDrgWeightRowVO> drgWeights();
}

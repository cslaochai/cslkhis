package com.his.medicaltech.mapper;

import com.his.medicaltech.vo.StatsOverviewVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 报表统计聚合 Mapper（跨模块只读，全部裸 SQL，一处收口；列名已按 information_schema 逐字核对）。
 *
 * <p>公共口径：
 * <ul>
 *   <li>start/end 为 'yyyy-MM-dd' 字符串，datetime 列比较时右侧补 ' 00:00:00'/' 23:59:59' 全天边界；</li>
 *   <li>出院队列 = discharge_time 非空且落区间、排除 7-医嘱取消入院（dev 库有"有出院时间但状态未流转"脏行，
 *       不以 admit_status 判出院，与 StatReportAggMapper 同源）；</li>
 *   <li>收入净额 = L1 记账行带符号合计（记账金额求和，红冲写负行、金额不再 UPDATE）；</li>
 *   <li>支付构成与退费一律取 L3 流水支付资金流水（收正退负，txn_status=1）；</li>
 *   <li>注解值必须是编译期常量，用 + 拼接。</li>
 * </ul>
 */
@Mapper
public interface StatsMapper {

    String NET = "SUM(f.amount)";

    String FEE_FROM = "FROM biz_fee_record f WHERE f.del_flag = 0 ";
    String FEE_RANGE = "AND f.book_time >= CONCAT(#{start}, ' 00:00:00') "
            + "AND f.book_time <= CONCAT(#{end}, ' 23:59:59') ";

    // 门诊（挂号信息，时间轴 visit_date）
    // 口径：regist_status 5-已退号不计入门诊量；初/复诊只认 visit_type（revisit_type 已废弃全 NULL）
    String DISCHARGE_WHERE = "a.del_flag = 0 AND a.admit_status <> 7 AND a.discharge_time IS NOT NULL "
            + "AND a.discharge_time >= CONCAT(#{start}, ' 00:00:00') "
            + "AND a.discharge_time <= CONCAT(#{end}, ' 23:59:59') ";

    @Select("SELECT COUNT(*) FROM biz_appoint_info "
            + "WHERE del_flag = 0 AND visit_date BETWEEN #{start} AND #{end}")
    Long countRegistTotal(@Param("start") String start, @Param("end") String end);

    @Select("SELECT COUNT(*) FROM biz_appoint_info "
            + "WHERE del_flag = 0 AND visit_date BETWEEN #{start} AND #{end} AND regist_status <> 5")
    Long countVisit(@Param("start") String start, @Param("end") String end);

    @Select("SELECT COUNT(*) FROM biz_appoint_info "
            + "WHERE del_flag = 0 AND visit_date BETWEEN #{start} AND #{end} AND regist_status = 5")
    Long countCancel(@Param("start") String start, @Param("end") String end);

    @Select("SELECT COUNT(*) FROM biz_appoint_info "
            + "WHERE del_flag = 0 AND visit_date BETWEEN #{start} AND #{end} AND regist_status <> 5 AND visit_type = 1")
    Long countFirstVisit(@Param("start") String start, @Param("end") String end);

    @Select("SELECT COUNT(*) FROM biz_appoint_info "
            + "WHERE del_flag = 0 AND visit_date BETWEEN #{start} AND #{end} AND regist_status <> 5 AND visit_type = 2")
    Long countRevisit(@Param("start") String start, @Param("end") String end);

    @Select("SELECT COUNT(*) FROM biz_appoint_info "
            + "WHERE del_flag = 0 AND visit_date BETWEEN #{start} AND #{end} AND regist_status <> 5 "
            + "AND settlement_type IS NOT NULL AND settlement_type <> 1")
    Long countInsurance(@Param("start") String start, @Param("end") String end);

    @Select("SELECT DATE_FORMAT(visit_date, '%m-%d') AS d, "
            + "SUM(CASE WHEN regist_status <> 5 THEN 1 ELSE 0 END) AS n, "
            + "SUM(CASE WHEN regist_status = 5 THEN 1 ELSE 0 END) AS cancels "
            + "FROM biz_appoint_info WHERE del_flag = 0 AND visit_date BETWEEN #{start} AND #{end} "
            + "GROUP BY visit_date ORDER BY visit_date")
    List<StatsOverviewVO.OpTrendRow> opTrend(@Param("start") String start, @Param("end") String end);

    @Select("SELECT IFNULL(dept_name, '未分配科室') AS name, COUNT(*) AS cnt "
            + "FROM biz_appoint_info WHERE del_flag = 0 AND visit_date BETWEEN #{start} AND #{end} "
            + "AND regist_status <> 5 GROUP BY name ORDER BY cnt DESC LIMIT 10")
    List<StatsOverviewVO.NameCountRow> opDeptTop(@Param("start") String start, @Param("end") String end);

    @Select("SELECT regist_type AS code, COUNT(*) AS cnt FROM biz_appoint_info "
            + "WHERE del_flag = 0 AND visit_date BETWEEN #{start} AND #{end} AND regist_status <> 5 "
            + "GROUP BY code ORDER BY code")
    List<StatsOverviewVO.CodeCountRow> opTypeDist(@Param("start") String start, @Param("end") String end);

    @Select("SELECT regist_source AS code, COUNT(*) AS cnt FROM biz_appoint_info "
            + "WHERE del_flag = 0 AND visit_date BETWEEN #{start} AND #{end} AND regist_status <> 5 "
            + "GROUP BY code ORDER BY code")
    List<StatsOverviewVO.CodeCountRow> opSourceDist(@Param("start") String start, @Param("end") String end);

    @Select("SELECT settlement_type AS code, COUNT(*) AS cnt FROM biz_appoint_info "
            + "WHERE del_flag = 0 AND visit_date BETWEEN #{start} AND #{end} AND regist_status <> 5 "
            + "GROUP BY code ORDER BY code")
    List<StatsOverviewVO.CodeCountRow> opSettleDist(@Param("start") String start, @Param("end") String end);

    // 住院（入院记录）

    @Select("SELECT CASE WHEN age IS NULL THEN '未填' WHEN age < 1 THEN '婴儿(<1)' "
            + "WHEN age <= 14 THEN '儿童(1-14)' WHEN age <= 40 THEN '青年(15-40)' "
            + "WHEN age <= 65 THEN '中年(41-65)' ELSE '老年(65+)' END AS name, COUNT(*) AS cnt "
            + "FROM biz_appoint_info WHERE del_flag = 0 AND visit_date BETWEEN #{start} AND #{end} "
            + "AND regist_status <> 5 GROUP BY name ORDER BY cnt DESC")
    List<StatsOverviewVO.NameCountRow> opAgeDist(@Param("start") String start, @Param("end") String end);

    @Select("SELECT COUNT(*) FROM biz_admission "
            + "WHERE del_flag = 0 AND admit_time >= CONCAT(#{start}, ' 00:00:00') "
            + "AND admit_time <= CONCAT(#{end}, ' 23:59:59')")
    Long countAdmit(@Param("start") String start, @Param("end") String end);

    @Select("SELECT COUNT(*) FROM biz_admission a WHERE " + DISCHARGE_WHERE)
    Long countDischarge(@Param("start") String start, @Param("end") String end);

    @Select("SELECT IFNULL(SUM(GREATEST(TIMESTAMPDIFF(MINUTE, a.admit_time, a.discharge_time), 0) / 1440), 0) "
            + "FROM biz_admission a WHERE " + DISCHARGE_WHERE)
    BigDecimal sumBedDays(@Param("start") String start, @Param("end") String end);

    @Select("SELECT COUNT(*) FROM biz_admission WHERE del_flag = 0 "
            + "AND admit_time <= CONCAT(#{end}, ' 23:59:59') "
            + "AND (discharge_time IS NULL OR discharge_time > CONCAT(#{end}, ' 23:59:59'))")
    Long countInHospitalAt(@Param("start") String start, @Param("end") String end);

    /**
     * 在院与占用床位都是「当前时点」快照，不支持回看历史日期
     */
    @Select("SELECT IFNULL(SUM(CASE WHEN bed_status = 2 THEN 1 ELSE 0 END), 0) AS occupied, "
            + "IFNULL(SUM(CASE WHEN bed_status <> 0 THEN 1 ELSE 0 END), 0) AS total "
            + "FROM sys_bed WHERE del_flag = 0")
    StatsOverviewVO.BedStat bedStat();

    @Select("SELECT DATE_FORMAT(d.dt, '%m-%d') AS d, SUM(d.admits) AS admits, SUM(d.discharges) AS discharges "
            + "FROM (SELECT DATE(admit_time) dt, COUNT(*) admits, 0 discharges FROM biz_admission "
            + "WHERE del_flag = 0 AND admit_time >= CONCAT(#{start}, ' 00:00:00') "
            + "AND admit_time <= CONCAT(#{end}, ' 23:59:59') GROUP BY dt "
            + "UNION ALL "
            + "SELECT DATE(discharge_time) dt, 0 admits, COUNT(*) discharges FROM biz_admission a "
            + "WHERE " + DISCHARGE_WHERE + " GROUP BY DATE(discharge_time)) d "
            + "GROUP BY d.dt ORDER BY d.dt")
    List<StatsOverviewVO.IpTrendRow> ipTrend(@Param("start") String start, @Param("end") String end);

    @Select("SELECT IFNULL(d.dept_name, '未分配科室') AS dept_name, "
            + "SUM(CASE WHEN a.admit_time >= CONCAT(#{start}, ' 00:00:00') "
            + "AND a.admit_time <= CONCAT(#{end}, ' 23:59:59') THEN 1 ELSE 0 END) AS admits, "
            + "COUNT(*) AS discharges, "
            + "ROUND(AVG(GREATEST(TIMESTAMPDIFF(MINUTE, a.admit_time, a.discharge_time), 0) / 1440), 1) AS avg_los "
            + "FROM biz_admission a LEFT JOIN sys_department d ON d.id = a.dept_id AND d.del_flag = 0 "
            + "WHERE " + DISCHARGE_WHERE
            + "GROUP BY dept_name ORDER BY discharges DESC LIMIT 10")
    List<StatsOverviewVO.IpDeptRow> ipDischargeDeptTop(@Param("start") String start, @Param("end") String end);

    // 住院结算（L2 出院结算账单 bill_type=4，排除 4-已作废）

    @Select("SELECT COUNT(*) AS settle_count, IFNULL(SUM(s.total_amount), 0) AS total_amount, "
            + "IFNULL(SUM(s.pool_amount), 0) AS insurance_amount, "
            + "IFNULL(SUM(s.payable_amount), 0) AS patient_pay_amount, "
            + "IFNULL(SUM(GREATEST(s.payable_amount - s.paid_amount, 0)), 0) AS arrears_amount "
            + "FROM biz_settlement_bill s "
            + "WHERE s.del_flag = 0 AND s.encounter_type = 2 AND s.bill_type = 4 AND s.bill_status <> 4 "
            + "AND s.bill_time >= CONCAT(#{start}, ' 00:00:00') "
            + "AND s.bill_time <= CONCAT(#{end}, ' 23:59:59')")
    StatsOverviewVO.IpSettleSummary ipSettle(@Param("start") String start, @Param("end") String end);

    @Select("SELECT IFNULL(NULLIF(s.insurance_type, ''), IF(s.settlement_mode = 2, '医保未登记', '自费')) AS name, "
            + "COUNT(*) AS cnt "
            + "FROM biz_settlement_bill s "
            + "WHERE s.del_flag = 0 AND s.encounter_type = 2 AND s.bill_type = 4 AND s.bill_status <> 4 "
            + "AND s.bill_time >= CONCAT(#{start}, ' 00:00:00') "
            + "AND s.bill_time <= CONCAT(#{end}, ' 23:59:59') "
            + "GROUP BY name ORDER BY cnt DESC")
    List<StatsOverviewVO.NameCountRow> ipInsuranceDist(@Param("start") String start, @Param("end") String end);

    // 收入（L1 记账费用记账流水明细口径）

    @Select("SELECT IFNULL(ROUND(" + NET + ", 2), 0) " + FEE_FROM + FEE_RANGE)
    BigDecimal sumRevenue(@Param("start") String start, @Param("end") String end);

    @Select("SELECT IFNULL(ROUND(" + NET + ", 2), 0) " + FEE_FROM + "AND f.item_type IN (2, 3, 4) " + FEE_RANGE)
    BigDecimal sumDrugRevenue(@Param("start") String start, @Param("end") String end);

    /**
     * item_type=8 耗材材料（字典 his_charge_item_type），与门诊/住院正交
     */
    @Select("SELECT IFNULL(ROUND(" + NET + ", 2), 0) " + FEE_FROM + "AND f.item_type = 8 " + FEE_RANGE)
    BigDecimal sumMaterialRevenue(@Param("start") String start, @Param("end") String end);

    /**
     * 门诊/住院按 encounter_type 分（1-门诊 2-住院），不再靠 source_id 是否为空猜
     */
    @Select("SELECT IFNULL(ROUND(" + NET + ", 2), 0) " + FEE_FROM + "AND f.encounter_type = 1 " + FEE_RANGE)
    BigDecimal sumOutpatientRevenue(@Param("start") String start, @Param("end") String end);

    @Select("SELECT IFNULL(ROUND(" + NET + ", 2), 0) " + FEE_FROM + "AND f.encounter_type = 2 " + FEE_RANGE)
    BigDecimal sumInpatientRevenue(@Param("start") String start, @Param("end") String end);

    @Select("SELECT DATE_FORMAT(f.book_time, '%m-%d') AS d, ROUND(" + NET + ", 2) AS amt "
            + FEE_FROM + FEE_RANGE
            + "GROUP BY DATE_FORMAT(f.book_time, '%m-%d') ORDER BY DATE_FORMAT(f.book_time, '%m-%d')")
    List<StatsOverviewVO.DateAmountRow> revTrend(@Param("start") String start, @Param("end") String end);

    @Select("SELECT CASE f.item_type WHEN 1 THEN '挂号费' WHEN 2 THEN '西药' WHEN 3 THEN '中成药' "
            + "WHEN 4 THEN '中药饮片' WHEN 5 THEN '检查' WHEN 6 THEN '检验' WHEN 7 THEN '治疗' WHEN 8 THEN '耗材材料' "
            + "ELSE CONCAT('其他(', f.item_type, ')') END AS name, "
            + "ROUND(" + NET + ", 2) AS amt "
            + FEE_FROM + FEE_RANGE
            + "GROUP BY f.item_type ORDER BY f.item_type")
    List<StatsOverviewVO.NameAmountRow> revTypeDist(@Param("start") String start, @Param("end") String end);

    @Select("SELECT IFNULL(f.dept_name, '未分配科室') AS name, ROUND(" + NET + ", 2) AS amt "
            + FEE_FROM + FEE_RANGE
            + "GROUP BY f.dept_name ORDER BY amt DESC LIMIT 10")
    List<StatsOverviewVO.NameAmountRow> revDeptTop(@Param("start") String start, @Param("end") String end);

    /**
     * 支付方式构成（L3 流水口径）：一笔钱一行，amount 收正退负，
     * 所以同一渠道的退款会把该渠道金额往回冲，笔数是流水笔数而不是收费单数。
     */
    @Select("SELECT t.pay_method AS code, COUNT(*) AS cnt, IFNULL(ROUND(SUM(t.amount), 2), 0) AS amt "
            + "FROM biz_payment_txn t "
            + "WHERE t.del_flag = 0 AND t.txn_status = 1 "
            + "AND t.txn_time >= CONCAT(#{start}, ' 00:00:00') "
            + "AND t.txn_time <= CONCAT(#{end}, ' 23:59:59') "
            + "GROUP BY t.pay_method ORDER BY t.pay_method")
    List<StatsOverviewVO.CodeCountAmountRow> revPayDist(@Param("start") String start, @Param("end") String end);

    /**
     * 期间退费额（取退款流水绝对值）与退款流水笔数
     */
    @Select("SELECT IFNULL(ROUND(SUM(-t.amount), 2), 0) FROM biz_payment_txn t "
            + "WHERE t.del_flag = 0 AND t.txn_status = 1 AND t.direction = 2 "
            + "AND t.txn_time >= CONCAT(#{start}, ' 00:00:00') "
            + "AND t.txn_time <= CONCAT(#{end}, ' 23:59:59')")
    BigDecimal sumRefundAmount(@Param("start") String start, @Param("end") String end);

    @Select("SELECT COUNT(*) FROM biz_payment_txn t "
            + "WHERE t.del_flag = 0 AND t.txn_status = 1 AND t.direction = 2 "
            + "AND t.txn_time >= CONCAT(#{start}, ' 00:00:00') "
            + "AND t.txn_time <= CONCAT(#{end}, ' 23:59:59')")
    Long countRefundBill(@Param("start") String start, @Param("end") String end);

    // 药事

    @Select("SELECT COUNT(*) FROM biz_prescription "
            + "WHERE del_flag = 0 AND visit_date BETWEEN #{start} AND #{end} "
            + "AND prescription_status >= 2 AND prescription_status <> 5")
    Long countPresc(@Param("start") String start, @Param("end") String end);

    @Select("SELECT COUNT(*) FROM biz_prescription "
            + "WHERE del_flag = 0 AND visit_date BETWEEN #{start} AND #{end} "
            + "AND prescription_status >= 2 AND prescription_status <> 5 AND prescription_source = #{source}")
    Long countPrescBySource(@Param("start") String start, @Param("end") String end,
                            @Param("source") Integer source);

    @Select("SELECT prescription_type AS code, COUNT(*) AS cnt, IFNULL(SUM(total_amount), 0) AS amt "
            + "FROM biz_prescription "
            + "WHERE del_flag = 0 AND visit_date BETWEEN #{start} AND #{end} "
            + "AND prescription_status >= 2 AND prescription_status <> 5 "
            + "GROUP BY code ORDER BY code")
    List<StatsOverviewVO.CodeCountAmountRow> phPrescTypeDist(@Param("start") String start, @Param("end") String end);

    @Select("SELECT IFNULL(SUM(return_count), 0) FROM biz_prescription "
            + "WHERE del_flag = 0 AND visit_date BETWEEN #{start} AND #{end}")
    Long sumPrescReturnCount(@Param("start") String start, @Param("end") String end);

    @Select("SELECT d.drug_name, IFNULL(d.specification, '') AS specification, IFNULL(d.unit, '') AS unit, "
            + "SUM(d.quantity) AS qty, ROUND(SUM(d.amount), 2) AS amt "
            + "FROM biz_prescription_detail d JOIN biz_prescription p "
            + "ON p.id = d.prescription_id AND p.del_flag = 0 "
            + "WHERE d.del_flag = 0 AND p.visit_date BETWEEN #{start} AND #{end} "
            + "AND p.prescription_status >= 2 AND p.prescription_status <> 5 "
            + "AND IFNULL(d.detail_status, 1) <> 3 "
            + "GROUP BY d.drug_name, specification, unit ORDER BY amt DESC LIMIT 10")
    List<StatsOverviewVO.DrugRankRow> phDrugTop(@Param("start") String start, @Param("end") String end);

    @Select("SELECT DATE_FORMAT(dispensing_time, '%m-%d') AS d, ROUND(IFNULL(SUM(amount), 0), 2) AS amt "
            + "FROM biz_drug_dispensing "
            + "WHERE del_flag = 0 AND dispensing_status = 2 "
            + "AND dispensing_time >= CONCAT(#{start}, ' 00:00:00') "
            + "AND dispensing_time <= CONCAT(#{end}, ' 23:59:59') "
            + "GROUP BY d ORDER BY d")
    List<StatsOverviewVO.DateAmountRow> phDispTrend(@Param("start") String start, @Param("end") String end);

    @Select("SELECT COUNT(*) AS cnt, IFNULL(ROUND(SUM(amount), 2), 0) AS amt "
            + "FROM biz_drug_dispensing "
            + "WHERE del_flag = 0 AND dispensing_status = 3 "
            + "AND dispensing_time >= CONCAT(#{start}, ' 00:00:00') "
            + "AND dispensing_time <= CONCAT(#{end}, ' 23:59:59')")
    StatsOverviewVO.ReturnStat phReturnStat(@Param("start") String start, @Param("end") String end);
}

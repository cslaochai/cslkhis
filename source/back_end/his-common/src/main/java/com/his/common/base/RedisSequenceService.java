package com.his.common.base;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


/**
 * 基于 Redis 的分布式序列号生成服务
 * key 格式：{module}:{yyyyMMdd}，过期时间 24 小时
 */
@Service
@RequiredArgsConstructor
public class RedisSequenceService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 获取下一个序列号
     *
     * @param module 模块标识，如 PATIENT、QUEUE
     * @return 自增后的序列号（从 1 开始）
     */
    public long next(String module) {
        String key = module + ":" + LocalDate.now().format(DATE_FMT);
        Long seq = stringRedisTemplate.opsForValue().increment(key);
        if (seq != null && seq == 1L) {
            // 首次设置，设置 24 小时过期
            stringRedisTemplate.expire(key, Duration.ofHours(24));
        }
        return seq != null ? seq : 1L;
    }


    /**
     * 生成患者号：P + 年月日 + 5位流水号（Redis 递增，24小时过期）
     */
    public String generatePatientNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.PATIENT_NO_KEY_PREFIX);
        return Constants.PATIENT_NO_PREFIX + dateStr + String.format("%05d", seq);
    }

    /**
     * 生成角色编号
     * 格式：R + 年月日 + 4位流水号，（Redis 递增，24小时过期）
     */
    public String generateRoleCode() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.ROLE_NO_KEY_PREFIX);
        return Constants.ROLE_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    /**
     * 生成挂号单号：A + 年月日时分秒 + 4位流水号
     */
    public String generateAppointNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.APPOINT_NO_KEY_PREFIX);
        return Constants.APPOINT_NO_PREFIX + dateStr + String.format("%05d", seq);
    }

    /**
     * 生成员工工号：A + 年月日时分秒 + 4位流水号
     */
    public String generateEmployeeNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.EMPLOYEE_NO_KEY_PREFIX);
        return Constants.EMPLOYEE_NO_PREFIX + dateStr + String.format("%05d", seq);
    }

    /**
     * 生成急诊号：JZ + 年月日 + 5位流水号
     */
    public String generateEmergencyNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.EMERGENCY_NO_KEY_PREFIX);
        return Constants.EMERGENCY_NO_PREFIX + dateStr + String.format("%05d", seq);
    }

    /**
     * 生成采购订单号：CG + 年月日 + 4位流水号
     * 单号一经生成即固化（订单表 order_no 有唯一索引），不随订单编辑而变。
     */
    public String generatePurchaseNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.PURCHASE_NO_KEY_PREFIX);
        return Constants.PURCHASE_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    /**
     * 生成药品入库单号：IN + 年月日 + 3位流水号
     * 与 sql/7 铺底单号（IN20240101001）同格式，避免新旧单号看不出是一类单据。
     */
    public String generateInboundNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.INBOUND_NO_KEY_PREFIX);
        return Constants.INBOUND_NO_PREFIX + dateStr + String.format("%03d", seq);
    }

    /**
     * 生成不良事件编号：AE + 年月日 + 4位流水号（Redis 递增，24小时过期）
     */
    public String generateAdverseEventNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.ADVERSE_EVENT_NO_KEY_PREFIX);
        return Constants.ADVERSE_EVENT_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    /**
     * 生成病案借阅/复印单号：BR + 年月日 + 4位流水号（Redis 递增，24小时过期）
     */
    public String generateArchiveBorrowNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.ARCHIVE_BORROW_NO_KEY_PREFIX);
        return Constants.ARCHIVE_BORROW_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    /**
     * 生成编码任务号：CT + 年月日 + 4位流水号（Redis 递增，24小时过期）
     */
    public String generateCodeTaskNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.CODE_TASK_NO_KEY_PREFIX);
        return Constants.CODE_TASK_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    /**
     * 生成住院摆药单号：WD + 年月日 + 4位流水号（Redis 递增，24小时过期）
     */
    public String generateWardDispenseNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.WARD_DISPENSE_NO_KEY_PREFIX);
        return Constants.WARD_DISPENSE_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    /**
     * 生成静配单号：PV + 年月日 + 4位流水号（Redis 递增，24小时过期）
     */
    public String generatePivasNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.PIVAS_NO_KEY_PREFIX);
        return Constants.PIVAS_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    /**
     * 生成药房盘点单号：PD + 年月日 + 4位流水号（Redis 递增，24小时过期）
     */
    public String generateStocktakeNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.STOCKTAKE_NO_KEY_PREFIX);
        return Constants.STOCKTAKE_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    /**
     * 生成药品调拨单号：TB + 年月日 + 4位流水号（Redis 递增，24小时过期，sql/154）
     */
    public String generateDrugTransferNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.DRUG_TRANSFER_NO_KEY_PREFIX);
        return Constants.DRUG_TRANSFER_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    /**
     * 生成药品供应商退货单号：TG + 年月日 + 4位流水号（Redis 递增，24小时过期，sql/154）
     */
    public String generateSupplierReturnNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.SUPPLIER_RETURN_NO_KEY_PREFIX);
        return Constants.SUPPLIER_RETURN_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    /**
     * 生成中药代煎单号：TCMD + 年月日 + 4位流水号（Redis 递增，24小时过期，sql/139）
     */
    public String generateTcmDecoctNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.TCM_DECOCT_NO_KEY_PREFIX);
        return Constants.TCM_DECOCT_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    /**
     * 生成三级质控流转单号：QCF + 年月日 + 4位流水号（Redis 递增，24小时过期）
     */
    public String generateRecordQcFlowNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.RECORD_QC_FLOW_NO_PREFIX);
        return Constants.RECORD_QC_FLOW_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    /**
     * 生成检查预约单号：YY + 年月日 + 4位流水号（Redis 递增，24小时过期）
     */
    public String generateExamAppointNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.EXAM_APPOINT_NO_KEY_PREFIX);
        return Constants.EXAM_APPOINT_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    /**
     * 生成门诊治疗申请单号：TAPPLY + 年月日 + 4位流水号（Redis 递增，24小时过期）
     */
    public String generateTreatmentApplyNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.TREATMENT_APPLY_NO_KEY_PREFIX);
        return Constants.TREATMENT_APPLY_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    /**
     * 待发药记录编号：DSP + yyyyMMdd + 4 位序号（原先是「DP + 时间戳 + 进程内 AtomicInteger」，多实例会撞号）
     */
    public String generateDispensingNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.DISPENSING_NO_KEY_PREFIX);
        return Constants.DISPENSING_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    /**
     * 传染病报卡编号：INF + yyyyMMdd + 4 位序号
     */
    public String generateInfectiousReportNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.INFECTIOUS_REPORT_NO_KEY_PREFIX);
        return Constants.INFECTIOUS_REPORT_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    /**
     * 院感病例编号：ICASE + yyyyMMdd + 4 位序号
     */
    public String generateInfectionCaseNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.INFECTION_CASE_NO_KEY_PREFIX);
        return Constants.INFECTION_CASE_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    /**
     * 死亡证明编号：DC + yyyyMMdd + 4 位序号（sql/157）
     */
    public String generateDeathCertNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.DEATH_CERT_NO_KEY_PREFIX);
        return Constants.DEATH_CERT_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    /**
     * 死亡登记号：RG + yyyyMMdd + 4 位序号（sql/157）
     */
    public String generateDeathRegisterNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.DEATH_REGISTER_NO_KEY_PREFIX);
        return Constants.DEATH_REGISTER_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    /**
     * 病危重通知单号：BT + yyyyMMdd + 4 位序号（sql/161）
     */
    public String generateCriticalNoticeNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.CRITICAL_NOTICE_NO_KEY_PREFIX);
        return Constants.CRITICAL_NOTICE_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    /**
     * 住院请假单号：LV + yyyyMMdd + 4 位序号（sql/162）
     */
    public String generateInpatientLeaveNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.LEAVE_RECORD_NO_KEY_PREFIX);
        return Constants.LEAVE_RECORD_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    /**
     * 临床路径入径单号：LP + yyyyMMdd + 4 位序号
     */
    public String generatePathwayNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.PATHWAY_NO_KEY_PREFIX);
        return Constants.PATHWAY_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    /**
     * 透析号：DP + yyyyMMdd + 4 位序号
     */
    public String generateDialysisPatientNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.DIALYSIS_PATIENT_NO_KEY_PREFIX);
        return Constants.DIALYSIS_PATIENT_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    /**
     * 透析单号：HD + yyyyMMdd + 4 位序号
     */
    public String generateDialysisSessionNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.DIALYSIS_SESSION_NO_KEY_PREFIX);
        return Constants.DIALYSIS_SESSION_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    /**
     * ICU 入科单号：ICU + yyyyMMdd + 4 位序号
     */
    public String generateIcuStayNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.ICU_STAY_NO_KEY_PREFIX);
        return Constants.ICU_STAY_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    /**
     * 目标性监测编号：IMON + yyyyMMdd + 4 位序号
     */
    public String generateInfectionMonitorNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.INFECTION_MONITOR_NO_KEY_PREFIX);
        return Constants.INFECTION_MONITOR_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    /**
     * 费用记账流水号：FR + yyyyMMdd + 5 位序号
     */
    public String generateFeeNo() {
        return dated(Constants.FEE_NO_PREFIX, Constants.FEE_NO_KEY_PREFIX);
    }

    /**
     * 结算账单号：SB + yyyyMMdd + 5 位序号
     */
    public String generateBillNo() {
        return dated(Constants.BILL_NO_PREFIX, Constants.BILL_NO_KEY_PREFIX);
    }

    /**
     * 收款流水号：PT + yyyyMMdd + 5 位序号
     */
    public String generatePayTxnNo() {
        return dated(Constants.PAY_TXN_NO_PREFIX, Constants.PAY_TXN_NO_KEY_PREFIX);
    }

    /**
     * 退款流水号：RT + yyyyMMdd + 5 位序号
     */
    public String generateRefundTxnNo() {
        return dated(Constants.REFUND_TXN_NO_PREFIX, Constants.REFUND_TXN_NO_KEY_PREFIX);
    }

    /**
     * 资金账户流水号：AT + yyyyMMdd + 5 位序号
     */
    public String generateFundTxnNo() {
        return dated(Constants.FUND_TXN_NO_PREFIX, Constants.FUND_TXN_NO_KEY_PREFIX);
    }

    /**
     * 发票号：IV + yyyyMMdd + 5 位序号（旧口径用时间戳+随机数，同秒并发会撞号且对账页无法判重）
     */
    public String generateInvoiceNo() {
        return dated(Constants.INVOICE_NO_PREFIX, Constants.INVOICE_NO_KEY_PREFIX);
    }

    /**
     * 医保结算清单号：IS + yyyyMMdd + 5 位序号（与发票号同因：旧的时间戳+随机数会撞号）
     */
    public String generateInsuranceSettlementNo() {
        return dated(Constants.ISB_NO_PREFIX, Constants.ISB_NO_KEY_PREFIX);
    }

    private String dated(String prefix, String module) {
        String dateStr = LocalDate.now().toString().replace("-", "");
        return prefix + dateStr + String.format("%05d", this.next(module));
    }

    /**
     * 飞检批次号：FI + yyyyMMdd + 4 位序号
     */
    public String generateYbInspectNo() {
        return datedShort(Constants.YB_INSPECT_NO_PREFIX, Constants.YB_INSPECT_NO_KEY_PREFIX);
    }

    /**
     * 扣款通知单号：DK + yyyyMMdd + 4 位序号
     */
    public String generateYbDeductNo() {
        return datedShort(Constants.YB_DEDUCT_NO_PREFIX, Constants.YB_DEDUCT_NO_KEY_PREFIX);
    }

    /**
     * 慢特病备案单号：MT + yyyyMMdd + 4 位序号
     */
    public String generateChronicRegNo() {
        return datedShort(Constants.CHRONIC_REG_NO_PREFIX, Constants.CHRONIC_REG_NO_KEY_PREFIX);
    }

    private String datedShort(String prefix, String module) {
        String dateStr = LocalDate.now().toString().replace("-", "");
        return prefix + dateStr + String.format("%04d", this.next(module));
    }
}

package com.his.common.service.impl;

import com.his.common.base.Constants;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.DateFormats;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;

/**
 * {@link RedisSequenceService} 的 Redis 实现。
 *
 * <p>每个业务一个 key（{@code {module}:{yyyyMMdd}}），首次自增时补 24 小时过期 ——
 * 不设过期会让 Redis 里堆满历史日的 key；设了过期又不必担心"当天 key 提前没了"，
 * 因为 24 小时 > 一天，跨天时新 key 会自然生成。
 */
@Service
@RequiredArgsConstructor
public class RedisSequenceServiceImpl implements RedisSequenceService {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public long next(String module) {
        String key = module + ":" + LocalDate.now().format(DateFormats.COMPACT_DATE);
        Long seq = stringRedisTemplate.opsForValue().increment(key);
        if (seq != null && seq == 1L) {
            // 首次设置，设置 24 小时过期
            stringRedisTemplate.expire(key, Duration.ofHours(24));
        }
        return seq != null ? seq : 1L;
    }

    @Override
    public String generatePatientNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.PATIENT_NO_KEY_PREFIX);
        return Constants.PATIENT_NO_PREFIX + dateStr + String.format("%05d", seq);
    }

    @Override
    public String generateRoleCode() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.ROLE_NO_KEY_PREFIX);
        return Constants.ROLE_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    @Override
    public String generateAppointNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.APPOINT_NO_KEY_PREFIX);
        return Constants.APPOINT_NO_PREFIX + dateStr + String.format("%05d", seq);
    }

    @Override
    public String generateEmployeeNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.EMPLOYEE_NO_KEY_PREFIX);
        return Constants.EMPLOYEE_NO_PREFIX + dateStr + String.format("%05d", seq);
    }

    @Override
    public String generateEmergencyNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.EMERGENCY_NO_KEY_PREFIX);
        return Constants.EMERGENCY_NO_PREFIX + dateStr + String.format("%05d", seq);
    }

    @Override
    public String generatePurchaseNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.PURCHASE_NO_KEY_PREFIX);
        return Constants.PURCHASE_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    @Override
    public String generateInboundNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.INBOUND_NO_KEY_PREFIX);
        return Constants.INBOUND_NO_PREFIX + dateStr + String.format("%03d", seq);
    }

    @Override
    public String generateAdverseEventNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.ADVERSE_EVENT_NO_KEY_PREFIX);
        return Constants.ADVERSE_EVENT_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    @Override
    public String generateArchiveBorrowNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.ARCHIVE_BORROW_NO_KEY_PREFIX);
        return Constants.ARCHIVE_BORROW_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    @Override
    public String generateCodeTaskNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.CODE_TASK_NO_KEY_PREFIX);
        return Constants.CODE_TASK_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    @Override
    public String generateWardDispenseNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.WARD_DISPENSE_NO_KEY_PREFIX);
        return Constants.WARD_DISPENSE_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    @Override
    public String generatePivasNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.PIVAS_NO_KEY_PREFIX);
        return Constants.PIVAS_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    @Override
    public String generateStocktakeNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.STOCKTAKE_NO_KEY_PREFIX);
        return Constants.STOCKTAKE_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    @Override
    public String generateDrugTransferNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.DRUG_TRANSFER_NO_KEY_PREFIX);
        return Constants.DRUG_TRANSFER_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    @Override
    public String generateSupplierReturnNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.SUPPLIER_RETURN_NO_KEY_PREFIX);
        return Constants.SUPPLIER_RETURN_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    @Override
    public String generateTcmDecoctNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.TCM_DECOCT_NO_KEY_PREFIX);
        return Constants.TCM_DECOCT_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    @Override
    public String generateRecordQcFlowNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.RECORD_QC_FLOW_NO_PREFIX);
        return Constants.RECORD_QC_FLOW_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    @Override
    public String generateExamAppointNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.EXAM_APPOINT_NO_KEY_PREFIX);
        return Constants.EXAM_APPOINT_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    @Override
    public String generateTreatmentApplyNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.TREATMENT_APPLY_NO_KEY_PREFIX);
        return Constants.TREATMENT_APPLY_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    @Override
    public String generateDispensingNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.DISPENSING_NO_KEY_PREFIX);
        return Constants.DISPENSING_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    @Override
    public String generateInfectiousReportNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.INFECTIOUS_REPORT_NO_KEY_PREFIX);
        return Constants.INFECTIOUS_REPORT_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    @Override
    public String generateInfectionCaseNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.INFECTION_CASE_NO_KEY_PREFIX);
        return Constants.INFECTION_CASE_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    @Override
    public String generateDeathCertNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.DEATH_CERT_NO_KEY_PREFIX);
        return Constants.DEATH_CERT_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    @Override
    public String generateDeathRegisterNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.DEATH_REGISTER_NO_KEY_PREFIX);
        return Constants.DEATH_REGISTER_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    @Override
    public String generateCriticalNoticeNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.CRITICAL_NOTICE_NO_KEY_PREFIX);
        return Constants.CRITICAL_NOTICE_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    @Override
    public String generateInpatientLeaveNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.LEAVE_RECORD_NO_KEY_PREFIX);
        return Constants.LEAVE_RECORD_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    @Override
    public String generatePathwayNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.PATHWAY_NO_KEY_PREFIX);
        return Constants.PATHWAY_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    @Override
    public String generateDialysisPatientNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.DIALYSIS_PATIENT_NO_KEY_PREFIX);
        return Constants.DIALYSIS_PATIENT_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    @Override
    public String generateDialysisSessionNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.DIALYSIS_SESSION_NO_KEY_PREFIX);
        return Constants.DIALYSIS_SESSION_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    @Override
    public String generateIcuStayNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.ICU_STAY_NO_KEY_PREFIX);
        return Constants.ICU_STAY_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    @Override
    public String generateInfectionMonitorNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = this.next(Constants.INFECTION_MONITOR_NO_KEY_PREFIX);
        return Constants.INFECTION_MONITOR_NO_PREFIX + dateStr + String.format("%04d", seq);
    }

    @Override
    public String generateFeeNo() {
        return dated(Constants.FEE_NO_PREFIX, Constants.FEE_NO_KEY_PREFIX);
    }

    @Override
    public String generateBillNo() {
        return dated(Constants.BILL_NO_PREFIX, Constants.BILL_NO_KEY_PREFIX);
    }

    @Override
    public String generatePayTxnNo() {
        return dated(Constants.PAY_TXN_NO_PREFIX, Constants.PAY_TXN_NO_KEY_PREFIX);
    }

    @Override
    public String generateRefundTxnNo() {
        return dated(Constants.REFUND_TXN_NO_PREFIX, Constants.REFUND_TXN_NO_KEY_PREFIX);
    }

    @Override
    public String generateFundTxnNo() {
        return dated(Constants.FUND_TXN_NO_PREFIX, Constants.FUND_TXN_NO_KEY_PREFIX);
    }

    @Override
    public String generateInvoiceNo() {
        return dated(Constants.INVOICE_NO_PREFIX, Constants.INVOICE_NO_KEY_PREFIX);
    }

    @Override
    public String generateInsuranceSettlementNo() {
        return dated(Constants.ISB_NO_PREFIX, Constants.ISB_NO_KEY_PREFIX);
    }

    private String dated(String prefix, String module) {
        String dateStr = LocalDate.now().toString().replace("-", "");
        return prefix + dateStr + String.format("%05d", this.next(module));
    }

    @Override
    public String generateYbInspectNo() {
        return datedShort(Constants.YB_INSPECT_NO_PREFIX, Constants.YB_INSPECT_NO_KEY_PREFIX);
    }

    @Override
    public String generateYbDeductNo() {
        return datedShort(Constants.YB_DEDUCT_NO_PREFIX, Constants.YB_DEDUCT_NO_KEY_PREFIX);
    }

    @Override
    public String generateChronicRegNo() {
        return datedShort(Constants.CHRONIC_REG_NO_PREFIX, Constants.CHRONIC_REG_NO_KEY_PREFIX);
    }

    private String datedShort(String prefix, String module) {
        String dateStr = LocalDate.now().toString().replace("-", "");
        return prefix + dateStr + String.format("%04d", this.next(module));
    }
}

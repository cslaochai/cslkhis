package com.his.common.service.impl;

import com.his.common.base.Constants;
import com.his.common.exception.BusinessException;
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
 * <p>单号的四件事（key、前缀、日期段、序号宽度）只在 {@link #no(String, String, int)} 拼装一次，
 * 每个 {@code generateXxxNo()} 只声明"用哪个前缀、哪个 key、几位"。
 */
@Service
@RequiredArgsConstructor
public class RedisSequenceServiceImpl implements RedisSequenceService {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public long next(String module) {
        String key = module + ":" + LocalDate.now().format(DateFormats.COMPACT_DATE);
        Long seq = stringRedisTemplate.opsForValue().increment(key);
        if (seq == null) {
            // 兜成 1 会发出一个与当天已有号重复的单号，撞唯一索引时报的是「数据重复」，
            // 真因（没取到号）就查不出来了
            throw new BusinessException("发号服务未返回序号：" + module);
        }
        if (seq == 1L) {
            stringRedisTemplate.expire(key, Duration.ofHours(24));
        }
        return seq;
    }

    private String no(String prefix, String module, int width) {
        return prefix + LocalDate.now().format(DateFormats.COMPACT_DATE)
                + String.format("%0" + width + "d", next(module));
    }

    @Override
    public String generatePatientNo() {
        return no(Constants.PATIENT_NO_PREFIX, Constants.PATIENT_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateRoleCode() {
        return no(Constants.ROLE_NO_PREFIX, Constants.ROLE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateAppointNo() {
        return no(Constants.APPOINT_NO_PREFIX, Constants.APPOINT_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateEmployeeNo() {
        return no(Constants.EMPLOYEE_NO_PREFIX, Constants.EMPLOYEE_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateEmergencyNo() {
        return no(Constants.EMERGENCY_NO_PREFIX, Constants.EMERGENCY_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generatePurchaseNo() {
        return no(Constants.PURCHASE_NO_PREFIX, Constants.PURCHASE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateInboundNo() {
        return no(Constants.INBOUND_NO_PREFIX, Constants.INBOUND_NO_KEY_PREFIX, 3);
    }

    @Override
    public String generateAdverseEventNo() {
        return no(Constants.ADVERSE_EVENT_NO_PREFIX, Constants.ADVERSE_EVENT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateArchiveBorrowNo() {
        return no(Constants.ARCHIVE_BORROW_NO_PREFIX, Constants.ARCHIVE_BORROW_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateCodeTaskNo() {
        return no(Constants.CODE_TASK_NO_PREFIX, Constants.CODE_TASK_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateWardDispenseNo() {
        return no(Constants.WARD_DISPENSE_NO_PREFIX, Constants.WARD_DISPENSE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generatePivasNo() {
        return no(Constants.PIVAS_NO_PREFIX, Constants.PIVAS_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateStocktakeNo() {
        return no(Constants.STOCKTAKE_NO_PREFIX, Constants.STOCKTAKE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateDrugTransferNo() {
        return no(Constants.DRUG_TRANSFER_NO_PREFIX, Constants.DRUG_TRANSFER_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateSupplierReturnNo() {
        return no(Constants.SUPPLIER_RETURN_NO_PREFIX, Constants.SUPPLIER_RETURN_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateTcmDecoctNo() {
        return no(Constants.TCM_DECOCT_NO_PREFIX, Constants.TCM_DECOCT_NO_KEY_PREFIX, 4);
    }

    /**
     * key 传的是前缀常量而不是 RECORD_QC_FLOW_NO_KEY_PREFIX，这是历史 Redis key（"QCF"），
     * 逐字保留：换 key = 当天计数器从 1 重启 = 与当天已发的 QCF 号撞唯一索引，且不报错。
     */
    @Override
    public String generateRecordQcFlowNo() {
        return no(Constants.RECORD_QC_FLOW_NO_PREFIX, Constants.RECORD_QC_FLOW_NO_PREFIX, 4);
    }

    @Override
    public String generateExamAppointNo() {
        return no(Constants.EXAM_APPOINT_NO_PREFIX, Constants.EXAM_APPOINT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateTreatmentApplyNo() {
        return no(Constants.TREATMENT_APPLY_NO_PREFIX, Constants.TREATMENT_APPLY_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateDispensingNo() {
        return no(Constants.DISPENSING_NO_PREFIX, Constants.DISPENSING_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateInfectiousReportNo() {
        return no(Constants.INFECTIOUS_REPORT_NO_PREFIX, Constants.INFECTIOUS_REPORT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateInfectionCaseNo() {
        return no(Constants.INFECTION_CASE_NO_PREFIX, Constants.INFECTION_CASE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateDeathCertNo() {
        return no(Constants.DEATH_CERT_NO_PREFIX, Constants.DEATH_CERT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateDeathRegisterNo() {
        return no(Constants.DEATH_REGISTER_NO_PREFIX, Constants.DEATH_REGISTER_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateCriticalNoticeNo() {
        return no(Constants.CRITICAL_NOTICE_NO_PREFIX, Constants.CRITICAL_NOTICE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateInpatientLeaveNo() {
        return no(Constants.LEAVE_RECORD_NO_PREFIX, Constants.LEAVE_RECORD_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generatePathwayNo() {
        return no(Constants.PATHWAY_NO_PREFIX, Constants.PATHWAY_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateDialysisPatientNo() {
        return no(Constants.DIALYSIS_PATIENT_NO_PREFIX, Constants.DIALYSIS_PATIENT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateDialysisSessionNo() {
        return no(Constants.DIALYSIS_SESSION_NO_PREFIX, Constants.DIALYSIS_SESSION_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateIcuStayNo() {
        return no(Constants.ICU_STAY_NO_PREFIX, Constants.ICU_STAY_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateInfectionMonitorNo() {
        return no(Constants.INFECTION_MONITOR_NO_PREFIX, Constants.INFECTION_MONITOR_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateFeeNo() {
        return no(Constants.FEE_NO_PREFIX, Constants.FEE_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateBillNo() {
        return no(Constants.BILL_NO_PREFIX, Constants.BILL_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generatePayTxnNo() {
        return no(Constants.PAY_TXN_NO_PREFIX, Constants.PAY_TXN_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateRefundTxnNo() {
        return no(Constants.REFUND_TXN_NO_PREFIX, Constants.REFUND_TXN_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateFundTxnNo() {
        return no(Constants.FUND_TXN_NO_PREFIX, Constants.FUND_TXN_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateInvoiceNo() {
        return no(Constants.INVOICE_NO_PREFIX, Constants.INVOICE_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateInsuranceSettlementNo() {
        return no(Constants.ISB_NO_PREFIX, Constants.ISB_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateYbInspectNo() {
        return no(Constants.YB_INSPECT_NO_PREFIX, Constants.YB_INSPECT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateYbDeductNo() {
        return no(Constants.YB_DEDUCT_NO_PREFIX, Constants.YB_DEDUCT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateChronicRegNo() {
        return no(Constants.CHRONIC_REG_NO_PREFIX, Constants.CHRONIC_REG_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateRuleCheckNo() {
        return no(Constants.RULE_CHECK_NO_PREFIX, Constants.RULE_CHECK_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateRuleCheckAiNo() {
        return no(Constants.RULE_CHECK_AI_NO_PREFIX, Constants.RULE_CHECK_AI_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateChronicRecordNo() {
        return no(Constants.CHRONIC_RECORD_NO_PREFIX, Constants.CHRONIC_RECORD_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateRefundApplyNo() {
        return no(Constants.REFUND_APPLY_NO_PREFIX, Constants.REFUND_APPLY_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateRxFlowNo() {
        return no(Constants.RX_FLOW_NO_PREFIX, Constants.RX_FLOW_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generatePublicHealthReportNo() {
        return no(Constants.PUBLIC_HEALTH_REPORT_NO_PREFIX, Constants.PUBLIC_HEALTH_REPORT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generatePrescriptionNo() {
        return no(Constants.PRESCRIPTION_NO_PREFIX, Constants.PRESCRIPTION_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateInspectionApplyNo() {
        return no(Constants.INSPECTION_APPLY_NO_PREFIX, Constants.INSPECTION_APPLY_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateLaboratoryApplyNo() {
        return no(Constants.LABORATORY_APPLY_NO_PREFIX, Constants.LABORATORY_APPLY_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateMedicalRecordNo() {
        return no(Constants.MEDICAL_RECORD_NO_PREFIX, Constants.MEDICAL_RECORD_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateFollowupTaskNo() {
        return no(Constants.FOLLOWUP_TASK_NO_PREFIX, Constants.FOLLOWUP_TASK_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateMedicalRecordArchiveNo() {
        return no(Constants.MEDICAL_RECORD_ARCHIVE_NO_PREFIX, Constants.MEDICAL_RECORD_ARCHIVE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateInspectionRecordNo() {
        return no(Constants.INSPECTION_RECORD_NO_PREFIX, Constants.INSPECTION_RECORD_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateLaboratoryRecordNo() {
        return no(Constants.LABORATORY_RECORD_NO_PREFIX, Constants.LABORATORY_RECORD_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateReportNo() {
        return no(Constants.REPORT_NO_PREFIX, Constants.REPORT_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateEcgWaveNo() {
        return no(Constants.ECG_WAVE_NO_PREFIX, Constants.ECG_WAVE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateMessageNo() {
        return no(Constants.MESSAGE_NO_PREFIX, Constants.MESSAGE_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateStatReportNo() {
        return no(Constants.STAT_REPORT_NO_PREFIX, Constants.STAT_REPORT_NO_KEY_PREFIX, 4);
    }
}

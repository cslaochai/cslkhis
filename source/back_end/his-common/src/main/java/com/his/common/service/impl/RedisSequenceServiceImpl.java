package com.his.common.service.impl;

import com.his.common.base.BizCodeConstants;
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
        return no(BizCodeConstants.PATIENT_NO_PREFIX, BizCodeConstants.PATIENT_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateRoleCode() {
        return no(BizCodeConstants.ROLE_NO_PREFIX, BizCodeConstants.ROLE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateAppointNo() {
        return no(BizCodeConstants.APPOINT_NO_PREFIX, BizCodeConstants.APPOINT_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateEmployeeNo() {
        return no(BizCodeConstants.EMPLOYEE_NO_PREFIX, BizCodeConstants.EMPLOYEE_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateEmergencyNo() {
        return no(BizCodeConstants.EMERGENCY_NO_PREFIX, BizCodeConstants.EMERGENCY_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generatePurchaseNo() {
        return no(BizCodeConstants.PURCHASE_NO_PREFIX, BizCodeConstants.PURCHASE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateInboundNo() {
        return no(BizCodeConstants.INBOUND_NO_PREFIX, BizCodeConstants.INBOUND_NO_KEY_PREFIX, 3);
    }

    @Override
    public String generateAdverseEventNo() {
        return no(BizCodeConstants.ADVERSE_EVENT_NO_PREFIX, BizCodeConstants.ADVERSE_EVENT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateArchiveBorrowNo() {
        return no(BizCodeConstants.ARCHIVE_BORROW_NO_PREFIX, BizCodeConstants.ARCHIVE_BORROW_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateCodeTaskNo() {
        return no(BizCodeConstants.CODE_TASK_NO_PREFIX, BizCodeConstants.CODE_TASK_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateWardDispenseNo() {
        return no(BizCodeConstants.WARD_DISPENSE_NO_PREFIX, BizCodeConstants.WARD_DISPENSE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generatePivasNo() {
        return no(BizCodeConstants.PIVAS_NO_PREFIX, BizCodeConstants.PIVAS_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateStocktakeNo() {
        return no(BizCodeConstants.STOCKTAKE_NO_PREFIX, BizCodeConstants.STOCKTAKE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateDrugTransferNo() {
        return no(BizCodeConstants.DRUG_TRANSFER_NO_PREFIX, BizCodeConstants.DRUG_TRANSFER_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateSupplierReturnNo() {
        return no(BizCodeConstants.SUPPLIER_RETURN_NO_PREFIX, BizCodeConstants.SUPPLIER_RETURN_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateTcmDecoctNo() {
        return no(BizCodeConstants.TCM_DECOCT_NO_PREFIX, BizCodeConstants.TCM_DECOCT_NO_KEY_PREFIX, 4);
    }

    /**
     * key 传的是前缀常量而不是 RECORD_QC_FLOW_NO_KEY_PREFIX，这是历史 Redis key（"QCF"），
     * 逐字保留：换 key = 当天计数器从 1 重启 = 与当天已发的 QCF 号撞唯一索引，且不报错。
     */
    @Override
    public String generateRecordQcFlowNo() {
        return no(BizCodeConstants.RECORD_QC_FLOW_NO_PREFIX, BizCodeConstants.RECORD_QC_FLOW_NO_PREFIX, 4);
    }

    @Override
    public String generateExamAppointNo() {
        return no(BizCodeConstants.EXAM_APPOINT_NO_PREFIX, BizCodeConstants.EXAM_APPOINT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateTreatmentApplyNo() {
        return no(BizCodeConstants.TREATMENT_APPLY_NO_PREFIX, BizCodeConstants.TREATMENT_APPLY_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateDispensingNo() {
        return no(BizCodeConstants.DISPENSING_NO_PREFIX, BizCodeConstants.DISPENSING_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateInfectiousReportNo() {
        return no(BizCodeConstants.INFECTIOUS_REPORT_NO_PREFIX, BizCodeConstants.INFECTIOUS_REPORT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateInfectionCaseNo() {
        return no(BizCodeConstants.INFECTION_CASE_NO_PREFIX, BizCodeConstants.INFECTION_CASE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateDeathCertNo() {
        return no(BizCodeConstants.DEATH_CERT_NO_PREFIX, BizCodeConstants.DEATH_CERT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateDeathRegisterNo() {
        return no(BizCodeConstants.DEATH_REGISTER_NO_PREFIX, BizCodeConstants.DEATH_REGISTER_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateCriticalNoticeNo() {
        return no(BizCodeConstants.CRITICAL_NOTICE_NO_PREFIX, BizCodeConstants.CRITICAL_NOTICE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateInpatientLeaveNo() {
        return no(BizCodeConstants.LEAVE_RECORD_NO_PREFIX, BizCodeConstants.LEAVE_RECORD_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generatePathwayNo() {
        return no(BizCodeConstants.PATHWAY_NO_PREFIX, BizCodeConstants.PATHWAY_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateDialysisPatientNo() {
        return no(BizCodeConstants.DIALYSIS_PATIENT_NO_PREFIX, BizCodeConstants.DIALYSIS_PATIENT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateDialysisSessionNo() {
        return no(BizCodeConstants.DIALYSIS_SESSION_NO_PREFIX, BizCodeConstants.DIALYSIS_SESSION_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateIcuStayNo() {
        return no(BizCodeConstants.ICU_STAY_NO_PREFIX, BizCodeConstants.ICU_STAY_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateInfectionMonitorNo() {
        return no(BizCodeConstants.INFECTION_MONITOR_NO_PREFIX, BizCodeConstants.INFECTION_MONITOR_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateFeeNo() {
        return no(BizCodeConstants.FEE_NO_PREFIX, BizCodeConstants.FEE_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateBillNo() {
        return no(BizCodeConstants.BILL_NO_PREFIX, BizCodeConstants.BILL_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generatePayTxnNo() {
        return no(BizCodeConstants.PAY_TXN_NO_PREFIX, BizCodeConstants.PAY_TXN_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateRefundTxnNo() {
        return no(BizCodeConstants.REFUND_TXN_NO_PREFIX, BizCodeConstants.REFUND_TXN_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateFundTxnNo() {
        return no(BizCodeConstants.FUND_TXN_NO_PREFIX, BizCodeConstants.FUND_TXN_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateInvoiceNo() {
        return no(BizCodeConstants.INVOICE_NO_PREFIX, BizCodeConstants.INVOICE_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateInsuranceSettlementNo() {
        return no(BizCodeConstants.ISB_NO_PREFIX, BizCodeConstants.ISB_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateYbInspectNo() {
        return no(BizCodeConstants.YB_INSPECT_NO_PREFIX, BizCodeConstants.YB_INSPECT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateYbDeductNo() {
        return no(BizCodeConstants.YB_DEDUCT_NO_PREFIX, BizCodeConstants.YB_DEDUCT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateChronicRegNo() {
        return no(BizCodeConstants.CHRONIC_REG_NO_PREFIX, BizCodeConstants.CHRONIC_REG_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateRuleCheckNo() {
        return no(BizCodeConstants.RULE_CHECK_NO_PREFIX, BizCodeConstants.RULE_CHECK_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateRuleCheckAiNo() {
        return no(BizCodeConstants.RULE_CHECK_AI_NO_PREFIX, BizCodeConstants.RULE_CHECK_AI_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateChronicRecordNo() {
        return no(BizCodeConstants.CHRONIC_RECORD_NO_PREFIX, BizCodeConstants.CHRONIC_RECORD_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateRefundApplyNo() {
        return no(BizCodeConstants.REFUND_APPLY_NO_PREFIX, BizCodeConstants.REFUND_APPLY_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateRxFlowNo() {
        return no(BizCodeConstants.RX_FLOW_NO_PREFIX, BizCodeConstants.RX_FLOW_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generatePublicHealthReportNo() {
        return no(BizCodeConstants.PUBLIC_HEALTH_REPORT_NO_PREFIX, BizCodeConstants.PUBLIC_HEALTH_REPORT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generatePrescriptionNo() {
        return no(BizCodeConstants.PRESCRIPTION_NO_PREFIX, BizCodeConstants.PRESCRIPTION_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateInspectionApplyNo() {
        return no(BizCodeConstants.INSPECTION_APPLY_NO_PREFIX, BizCodeConstants.INSPECTION_APPLY_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateLaboratoryApplyNo() {
        return no(BizCodeConstants.LABORATORY_APPLY_NO_PREFIX, BizCodeConstants.LABORATORY_APPLY_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateMedicalRecordNo() {
        return no(BizCodeConstants.MEDICAL_RECORD_NO_PREFIX, BizCodeConstants.MEDICAL_RECORD_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateFollowupTaskNo() {
        return no(BizCodeConstants.FOLLOWUP_TASK_NO_PREFIX, BizCodeConstants.FOLLOWUP_TASK_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateMedicalRecordArchiveNo() {
        return no(BizCodeConstants.MEDICAL_RECORD_ARCHIVE_NO_PREFIX, BizCodeConstants.MEDICAL_RECORD_ARCHIVE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateInspectionRecordNo() {
        return no(BizCodeConstants.INSPECTION_RECORD_NO_PREFIX, BizCodeConstants.INSPECTION_RECORD_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateLaboratoryRecordNo() {
        return no(BizCodeConstants.LABORATORY_RECORD_NO_PREFIX, BizCodeConstants.LABORATORY_RECORD_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateReportNo() {
        return no(BizCodeConstants.REPORT_NO_PREFIX, BizCodeConstants.REPORT_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateEcgWaveNo() {
        return no(BizCodeConstants.ECG_WAVE_NO_PREFIX, BizCodeConstants.ECG_WAVE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateMessageNo() {
        return no(BizCodeConstants.MESSAGE_NO_PREFIX, BizCodeConstants.MESSAGE_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateStatReportNo() {
        return no(BizCodeConstants.STAT_REPORT_NO_PREFIX, BizCodeConstants.STAT_REPORT_NO_KEY_PREFIX, 4);
    }
}

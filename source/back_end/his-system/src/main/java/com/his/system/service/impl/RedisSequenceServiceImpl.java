package com.his.system.service.impl;

import com.his.common.constant.BizCodeConst;
import com.his.common.exception.BusinessException;
import com.his.system.service.RedisSequenceService;
import com.his.common.util.DateFormats;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;

/**
 * RedisSequenceService 的 Redis 实现。
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
        return no(BizCodeConst.PATIENT_NO_PREFIX, BizCodeConst.PATIENT_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateRoleCode() {
        return no(BizCodeConst.ROLE_NO_PREFIX, BizCodeConst.ROLE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateAppointNo() {
        return no(BizCodeConst.APPOINT_NO_PREFIX, BizCodeConst.APPOINT_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateEmployeeNo() {
        return no(BizCodeConst.EMPLOYEE_NO_PREFIX, BizCodeConst.EMPLOYEE_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateEmergencyNo() {
        return no(BizCodeConst.EMERGENCY_NO_PREFIX, BizCodeConst.EMERGENCY_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generatePurchaseNo() {
        return no(BizCodeConst.PURCHASE_NO_PREFIX, BizCodeConst.PURCHASE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateInboundNo() {
        return no(BizCodeConst.INBOUND_NO_PREFIX, BizCodeConst.INBOUND_NO_KEY_PREFIX, 3);
    }

    @Override
    public String generateAdverseEventNo() {
        return no(BizCodeConst.ADVERSE_EVENT_NO_PREFIX, BizCodeConst.ADVERSE_EVENT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateArchiveBorrowNo() {
        return no(BizCodeConst.ARCHIVE_BORROW_NO_PREFIX, BizCodeConst.ARCHIVE_BORROW_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateCodeTaskNo() {
        return no(BizCodeConst.CODE_TASK_NO_PREFIX, BizCodeConst.CODE_TASK_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateWardDispenseNo() {
        return no(BizCodeConst.WARD_DISPENSE_NO_PREFIX, BizCodeConst.WARD_DISPENSE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generatePivasNo() {
        return no(BizCodeConst.PIVAS_NO_PREFIX, BizCodeConst.PIVAS_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateStocktakeNo() {
        return no(BizCodeConst.STOCKTAKE_NO_PREFIX, BizCodeConst.STOCKTAKE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateDrugTransferNo() {
        return no(BizCodeConst.DRUG_TRANSFER_NO_PREFIX, BizCodeConst.DRUG_TRANSFER_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateSupplierReturnNo() {
        return no(BizCodeConst.SUPPLIER_RETURN_NO_PREFIX, BizCodeConst.SUPPLIER_RETURN_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateTcmDecoctNo() {
        return no(BizCodeConst.TCM_DECOCT_NO_PREFIX, BizCodeConst.TCM_DECOCT_NO_KEY_PREFIX, 4);
    }

    /**
     * key 传的是前缀常量而不是 RECORD_QC_FLOW_NO_KEY_PREFIX，这是历史 Redis key（"QCF"），
     * 逐字保留：换 key = 当天计数器从 1 重启 = 与当天已发的 QCF 号撞唯一索引，且不报错。
     */
    @Override
    public String generateRecordQcFlowNo() {
        return no(BizCodeConst.RECORD_QC_FLOW_NO_PREFIX, BizCodeConst.RECORD_QC_FLOW_NO_PREFIX, 4);
    }

    @Override
    public String generateExamAppointNo() {
        return no(BizCodeConst.EXAM_APPOINT_NO_PREFIX, BizCodeConst.EXAM_APPOINT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateTreatmentApplyNo() {
        return no(BizCodeConst.TREATMENT_APPLY_NO_PREFIX, BizCodeConst.TREATMENT_APPLY_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateDispensingNo() {
        return no(BizCodeConst.DISPENSING_NO_PREFIX, BizCodeConst.DISPENSING_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateInfectiousReportNo() {
        return no(BizCodeConst.INFECTIOUS_REPORT_NO_PREFIX, BizCodeConst.INFECTIOUS_REPORT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateInfectionCaseNo() {
        return no(BizCodeConst.INFECTION_CASE_NO_PREFIX, BizCodeConst.INFECTION_CASE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateDeathCertNo() {
        return no(BizCodeConst.DEATH_CERT_NO_PREFIX, BizCodeConst.DEATH_CERT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateDeathRegisterNo() {
        return no(BizCodeConst.DEATH_REGISTER_NO_PREFIX, BizCodeConst.DEATH_REGISTER_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateCriticalNoticeNo() {
        return no(BizCodeConst.CRITICAL_NOTICE_NO_PREFIX, BizCodeConst.CRITICAL_NOTICE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateInpatientLeaveNo() {
        return no(BizCodeConst.LEAVE_RECORD_NO_PREFIX, BizCodeConst.LEAVE_RECORD_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generatePathwayNo() {
        return no(BizCodeConst.PATHWAY_NO_PREFIX, BizCodeConst.PATHWAY_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateDialysisPatientNo() {
        return no(BizCodeConst.DIALYSIS_PATIENT_NO_PREFIX, BizCodeConst.DIALYSIS_PATIENT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateDialysisSessionNo() {
        return no(BizCodeConst.DIALYSIS_SESSION_NO_PREFIX, BizCodeConst.DIALYSIS_SESSION_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateIcuStayNo() {
        return no(BizCodeConst.ICU_STAY_NO_PREFIX, BizCodeConst.ICU_STAY_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateInfectionMonitorNo() {
        return no(BizCodeConst.INFECTION_MONITOR_NO_PREFIX, BizCodeConst.INFECTION_MONITOR_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateFeeNo() {
        return no(BizCodeConst.FEE_NO_PREFIX, BizCodeConst.FEE_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateBillNo() {
        return no(BizCodeConst.BILL_NO_PREFIX, BizCodeConst.BILL_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generatePayTxnNo() {
        return no(BizCodeConst.PAY_TXN_NO_PREFIX, BizCodeConst.PAY_TXN_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateRefundTxnNo() {
        return no(BizCodeConst.REFUND_TXN_NO_PREFIX, BizCodeConst.REFUND_TXN_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateFundTxnNo() {
        return no(BizCodeConst.FUND_TXN_NO_PREFIX, BizCodeConst.FUND_TXN_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateInvoiceNo() {
        return no(BizCodeConst.INVOICE_NO_PREFIX, BizCodeConst.INVOICE_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateInsuranceSettlementNo() {
        return no(BizCodeConst.ISB_NO_PREFIX, BizCodeConst.ISB_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateYbInspectNo() {
        return no(BizCodeConst.YB_INSPECT_NO_PREFIX, BizCodeConst.YB_INSPECT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateYbDeductNo() {
        return no(BizCodeConst.YB_DEDUCT_NO_PREFIX, BizCodeConst.YB_DEDUCT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateChronicRegNo() {
        return no(BizCodeConst.CHRONIC_REG_NO_PREFIX, BizCodeConst.CHRONIC_REG_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateRuleCheckNo() {
        return no(BizCodeConst.RULE_CHECK_NO_PREFIX, BizCodeConst.RULE_CHECK_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateRuleCheckAiNo() {
        return no(BizCodeConst.RULE_CHECK_AI_NO_PREFIX, BizCodeConst.RULE_CHECK_AI_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateChronicRecordNo() {
        return no(BizCodeConst.CHRONIC_RECORD_NO_PREFIX, BizCodeConst.CHRONIC_RECORD_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateRefundApplyNo() {
        return no(BizCodeConst.REFUND_APPLY_NO_PREFIX, BizCodeConst.REFUND_APPLY_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateRxFlowNo() {
        return no(BizCodeConst.RX_FLOW_NO_PREFIX, BizCodeConst.RX_FLOW_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generatePublicHealthReportNo() {
        return no(BizCodeConst.PUBLIC_HEALTH_REPORT_NO_PREFIX, BizCodeConst.PUBLIC_HEALTH_REPORT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generatePrescriptionNo() {
        return no(BizCodeConst.PRESCRIPTION_NO_PREFIX, BizCodeConst.PRESCRIPTION_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateInspectionApplyNo() {
        return no(BizCodeConst.INSPECTION_APPLY_NO_PREFIX, BizCodeConst.INSPECTION_APPLY_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateLaboratoryApplyNo() {
        return no(BizCodeConst.LABORATORY_APPLY_NO_PREFIX, BizCodeConst.LABORATORY_APPLY_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateMedicalRecordNo() {
        return no(BizCodeConst.MEDICAL_RECORD_NO_PREFIX, BizCodeConst.MEDICAL_RECORD_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateFollowupTaskNo() {
        return no(BizCodeConst.FOLLOWUP_TASK_NO_PREFIX, BizCodeConst.FOLLOWUP_TASK_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateMedicalRecordArchiveNo() {
        return no(BizCodeConst.MEDICAL_RECORD_ARCHIVE_NO_PREFIX, BizCodeConst.MEDICAL_RECORD_ARCHIVE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateInspectionRecordNo() {
        return no(BizCodeConst.INSPECTION_RECORD_NO_PREFIX, BizCodeConst.INSPECTION_RECORD_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateLaboratoryRecordNo() {
        return no(BizCodeConst.LABORATORY_RECORD_NO_PREFIX, BizCodeConst.LABORATORY_RECORD_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateReportNo() {
        return no(BizCodeConst.REPORT_NO_PREFIX, BizCodeConst.REPORT_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateEcgWaveNo() {
        return no(BizCodeConst.ECG_WAVE_NO_PREFIX, BizCodeConst.ECG_WAVE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateMessageNo() {
        return no(BizCodeConst.MESSAGE_NO_PREFIX, BizCodeConst.MESSAGE_NO_KEY_PREFIX, 5);
    }

    @Override
    public String generateStatReportNo() {
        return no(BizCodeConst.STAT_REPORT_NO_PREFIX, BizCodeConst.STAT_REPORT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateTransfusionApplyNo() {
        return no(BizCodeConst.TRANSFUSION_APPLY_NO_PREFIX, BizCodeConst.TRANSFUSION_APPLY_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateCriticalValueNo() {
        return no(BizCodeConst.CRITICAL_VALUE_NO_PREFIX, BizCodeConst.CRITICAL_VALUE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateEmrQcNo() {
        return no(BizCodeConst.EMR_QC_NO_PREFIX, BizCodeConst.EMR_QC_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateFieldChangeBatchNo() {
        return no(BizCodeConst.FIELD_CHANGE_NO_PREFIX, BizCodeConst.FIELD_CHANGE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateComplianceAuditNo() {
        return no(BizCodeConst.COMPLIANCE_AUDIT_NO_PREFIX, BizCodeConst.COMPLIANCE_AUDIT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateDrugTraceNo() {
        return no(BizCodeConst.DRUG_TRACE_NO_PREFIX, BizCodeConst.DRUG_TRACE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateDrugUploadBatchNo() {
        return no(BizCodeConst.DRUG_UPLOAD_BATCH_NO_PREFIX, BizCodeConst.DRUG_UPLOAD_BATCH_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateHighValueTraceNo() {
        return no(BizCodeConst.HIGH_VALUE_TRACE_NO_PREFIX, BizCodeConst.HIGH_VALUE_TRACE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateConsumableStockLogNo() {
        return no(BizCodeConst.CONSUMABLE_STOCK_LOG_NO_PREFIX, BizCodeConst.CONSUMABLE_STOCK_LOG_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateOperationApplyNo() {
        return no(BizCodeConst.OPERATION_APPLY_NO_PREFIX, BizCodeConst.OPERATION_APPLY_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateOperationCountNo() {
        return no(BizCodeConst.OPERATION_COUNT_NO_PREFIX, BizCodeConst.OPERATION_COUNT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateAnesthesiaVisitNo() {
        return no(BizCodeConst.ANESTHESIA_VISIT_NO_PREFIX, BizCodeConst.ANESTHESIA_VISIT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateAnesthesiaRecordNo() {
        return no(BizCodeConst.ANESTHESIA_RECORD_NO_PREFIX, BizCodeConst.ANESTHESIA_RECORD_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateAnesthesiaFollowupNo() {
        return no(BizCodeConst.ANESTHESIA_FOLLOWUP_NO_PREFIX, BizCodeConst.ANESTHESIA_FOLLOWUP_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generatePacuNo() {
        return no(BizCodeConst.PACU_NO_PREFIX, BizCodeConst.PACU_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateOperationSafetyCheckNo() {
        return no(BizCodeConst.OPERATION_SAFETY_CHECK_NO_PREFIX, BizCodeConst.OPERATION_SAFETY_CHECK_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateAdmissionOrderNo() {
        return no(BizCodeConst.ADMISSION_ORDER_NO_PREFIX, BizCodeConst.ADMISSION_ORDER_NO_KEY_PREFIX, 3);
    }

    @Override
    public String generateBedWaitNo() {
        return no(BizCodeConst.BED_WAIT_NO_PREFIX, BizCodeConst.BED_WAIT_NO_KEY_PREFIX, 3);
    }

    @Override
    public String generateBedAllocateNo() {
        return no(BizCodeConst.BED_ALLOCATE_NO_PREFIX, BizCodeConst.BED_ALLOCATE_NO_KEY_PREFIX, 3);
    }

    @Override
    public String generateConsultationNo() {
        return no(BizCodeConst.CONSULTATION_NO_PREFIX, BizCodeConst.CONSULTATION_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateInpatientRecordNo() {
        return no(BizCodeConst.INPATIENT_RECORD_NO_PREFIX, BizCodeConst.INPATIENT_RECORD_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateNursingAssessNo() {
        return no(BizCodeConst.NURSING_ASSESS_NO_PREFIX, BizCodeConst.NURSING_ASSESS_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateNursingRecordNo() {
        return no(BizCodeConst.NURSING_RECORD_NO_PREFIX, BizCodeConst.NURSING_RECORD_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateAdmissionNo() {
        return no(BizCodeConst.ADMISSION_NO_PREFIX, BizCodeConst.ADMISSION_NO_KEY_PREFIX, 3);
    }

    @Override
    public String generateDischargeNo() {
        return no(BizCodeConst.DISCHARGE_NO_PREFIX, BizCodeConst.DISCHARGE_NO_KEY_PREFIX, 3);
    }

    @Override
    public String generateVisitNo() {
        return no(BizCodeConst.VISIT_NO_PREFIX, BizCodeConst.VISIT_NO_KEY_PREFIX, 3);
    }

    @Override
    public String generateInpatientOrderNo() {
        return no(BizCodeConst.INPATIENT_ORDER_NO_PREFIX, BizCodeConst.INPATIENT_ORDER_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateOrderGroupNo() {
        return no(BizCodeConst.ORDER_GROUP_NO_PREFIX, BizCodeConst.ORDER_GROUP_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateTransferNo() {
        return no(BizCodeConst.TRANSFER_NO_PREFIX, BizCodeConst.TRANSFER_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generatePatientMergeNo() {
        return no(BizCodeConst.PATIENT_MERGE_NO_PREFIX, BizCodeConst.PATIENT_MERGE_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateReferralNo() {
        return no(BizCodeConst.REFERRAL_NO_PREFIX, BizCodeConst.REFERRAL_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateDischargeDrugNo() {
        return no(BizCodeConst.DISCHARGE_DRUG_NO_PREFIX, BizCodeConst.DISCHARGE_DRUG_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateVtePreventNo() {
        return no(BizCodeConst.VTE_PREVENT_NO_PREFIX, BizCodeConst.VTE_PREVENT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateVteEventNo() {
        return no(BizCodeConst.VTE_EVENT_NO_PREFIX, BizCodeConst.VTE_EVENT_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateDietPlanNo() {
        return no(BizCodeConst.DIET_PLAN_NO_PREFIX, BizCodeConst.DIET_PLAN_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateNutritionScreenNo() {
        return no(BizCodeConst.NUTRITION_SCREEN_NO_PREFIX, BizCodeConst.NUTRITION_SCREEN_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateMealOrderNo() {
        return no(BizCodeConst.MEAL_ORDER_NO_PREFIX, BizCodeConst.MEAL_ORDER_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateCheckupRecordNo() {
        return no(BizCodeConst.CHECKUP_RECORD_NO_PREFIX, BizCodeConst.CHECKUP_RECORD_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateAntibioticAuthNo() {
        return no(BizCodeConst.ANTIBIOTIC_AUTH_NO_PREFIX, BizCodeConst.ANTIBIOTIC_AUTH_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateAntibioticReviewNo() {
        return no(BizCodeConst.ANTIBIOTIC_REVIEW_NO_PREFIX, BizCodeConst.ANTIBIOTIC_REVIEW_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateQcStoreNo() {
        return no(BizCodeConst.QUALITY_CONTROL_NO_PREFIX, BizCodeConst.QUALITY_CONTROL_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateRxReviewBatchNo() {
        return no(BizCodeConst.RX_REVIEW_BATCH_NO_PREFIX, BizCodeConst.RX_REVIEW_BATCH_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateRxDoctorTalkNo() {
        return no(BizCodeConst.RX_DOCTOR_TALK_NO_PREFIX, BizCodeConst.RX_DOCTOR_TALK_NO_KEY_PREFIX, 4);
    }

    @Override
    public String generateNarcoticRegisterNo() {
        return no(BizCodeConst.NARCOTIC_REGISTER_NO_PREFIX, BizCodeConst.NARCOTIC_REGISTER_NO_KEY_PREFIX, 4);
    }
}

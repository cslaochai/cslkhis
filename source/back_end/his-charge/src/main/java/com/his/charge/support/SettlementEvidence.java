package com.his.charge.support;


import com.his.charge.entity.BizInsuranceSettlement;
import com.his.charge.entity.BizSettlementBill;
import com.his.charge.entity.BizSettlementBillItem;
import com.his.charge.vo.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 一次就诊的「依据包」。
 */
@Data
public class SettlementEvidence {

    /**
     * 结算清单本身
     */
    private BizInsuranceSettlement settlement;

    /**
     * 挂号记录（锚点载体）
     */
    private RegistBriefVO regist;

    /**
     * 患者档案（性别/年龄，逻辑排他规则要用）
     */
    private PatientBriefVO patient;

    /**
     * 病历（诊断依据的主要来源）
     */
    private MedicalRecordBriefVO medicalRecord;

    /**
     * 本次就诊处方
     */
    private List<PrescriptionBriefVO> prescriptions = new ArrayList<>();

    /**
     * 处方明细（药品名，用于诊断依据匹配）
     */
    private List<PrescriptionDetailBriefVO> prescriptionDetails = new ArrayList<>();

    /**
     * 结算账单（清单的宿主，L2）
     */
    private BizSettlementBill bill;

    /**
     * 账单行（费用依据，逐项目名与类型）
     */
    private List<BizSettlementBillItem> billItems = new ArrayList<>();

    /**
     * 检验记录
     */
    private List<LaboratoryRecordBriefVO> labRecords = new ArrayList<>();

    /**
     * 检验结果（明细项名 + 结论，低编证据的主要来源）
     */
    private List<LabResultBriefVO> labResults = new ArrayList<>();

    /**
     * 检查记录
     */
    private List<InspectionRecordBriefVO> inspections = new ArrayList<>();

    /**
     * 缺什么依据。规则判定为「不适用」时，原因从这里取 ——
     * 让「没评估」永远带着理由，不允许静默变成「通过」。
     */
    private List<String> missing = new ArrayList<>();

    // 派生判断

    private static String join(String... parts) {
        return EvidenceKeywordMatcher.join(parts);
    }

    /**
     * 患者性别（1-男 2-女 9-未知）
     */
    public Integer gender() {
        if (patient != null && patient.getGender() != null) {
            return patient.getGender();
        }
        if (settlement != null && settlement.getGender() != null) {
            return settlement.getGender();
        }
        if (regist != null && regist.getGender() != null) {
            return regist.getGender();
        }
        return null;
    }

    /**
     * 患者年龄，null 表示未知
     */
    public Integer age() {
        if (patient != null && patient.getAge() != null) {
            return patient.getAge();
        }
        if (settlement != null && settlement.getAge() != null) {
            return settlement.getAge();
        }
        if (regist != null && regist.getAge() != null) {
            return regist.getAge();
        }
        return null;
    }

    /**
     * 病历叙述性文本合集（主诉+现病史+既往史+专科检查+辅助检查+诊疗计划）
     */
    public String recordNarrative() {
        if (medicalRecord == null) {
            return "";
        }
        MedicalRecordBriefVO r = medicalRecord;
        return join(r.getChiefComplaint(), r.getPresentIllness(), r.getPastHistory(),
                r.getSpecialistExam(), r.getAuxiliaryExam(), r.getTreatmentPlan(),
                r.getDiagnosisName(), r.getDiagnosis());
    }

    /**
     * 本次就诊所有可见的诊疗对象名称（账单行 + 处方药 + 检验 + 检查）
     */
    public String allOrderNames() {
        StringBuilder sb = new StringBuilder();
        sb.append(billItems.stream()
                .map(BizSettlementBillItem::getItemName).filter(n -> n != null)
                .collect(Collectors.joining(" ")));
        sb.append(' ');
        sb.append(prescriptionDetails.stream()
                .map(PrescriptionDetailBriefVO::getDrugName).filter(n -> n != null)
                .collect(Collectors.joining(" ")));
        sb.append(' ');
        sb.append(labRecords.stream()
                .map(LaboratoryRecordBriefVO::getLaboratoryItemName).filter(n -> n != null)
                .collect(Collectors.joining(" ")));
        sb.append(' ');
        sb.append(labResults.stream()
                .map(LabResultBriefVO::getLaboratoryItemName).filter(n -> n != null)
                .collect(Collectors.joining(" ")));
        sb.append(' ');
        sb.append(inspections.stream()
                .map(InspectionRecordBriefVO::getInspectionItemName).filter(n -> n != null)
                .collect(Collectors.joining(" ")));
        return sb.toString();
    }

    /**
     * 手术依据文本 = 病历叙述 + 全部诊疗项目名 + 手术性治疗收费名
     */
    public String surgicalEvidenceText() {
        return join(recordNarrative(), allOrderNames());
    }

    /**
     * 是否存在任何检验/检查
     */
    public boolean hasAnyLabOrInspection() {
        return !labRecords.isEmpty() || !labResults.isEmpty() || !inspections.isEmpty();
    }

    /**
     * 是否存在治疗性收费（itemType=7）
     */
    public boolean hasTreatmentCharge() {
        return billItems.stream().anyMatch(d -> d.getItemType() != null && d.getItemType() == 7);
    }

    /**
     * 是否存在手术性治疗收费（治疗类且名称含手术语义）
     */
    public List<BizSettlementBillItem> surgicalTreatmentCharges(String... keywords) {
        return billItems.stream()
                .filter(d -> d.getItemType() != null && d.getItemType() == 7 && d.getItemName() != null)
                .filter(d -> EvidenceKeywordMatcher
                        .hits(d.getItemName(), keywords))
                .collect(Collectors.toList());
    }

    /**
     * 实际总费用：清单优先，其次账单，最后按账单行汇总
     */
    public BigDecimal actualCost() {
        if (settlement != null && settlement.getTotalAmount() != null
                && settlement.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            return settlement.getTotalAmount();
        }
        if (bill != null && bill.getTotalAmount() != null) {
            return bill.getTotalAmount();
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (BizSettlementBillItem d : billItems) {
            if (d.getAmount() != null) {
                sum = sum.add(d.getAmount());
            }
        }
        return sum;
    }

    /**
     * 药品费合计（账单行 itemType 2/3/4）
     */
    public BigDecimal drugCost() {
        BigDecimal sum = BigDecimal.ZERO;
        for (BizSettlementBillItem d : billItems) {
            if (d.getItemType() != null && (d.getItemType() == 2 || d.getItemType() == 3 || d.getItemType() == 4)
                    && d.getAmount() != null) {
                sum = sum.add(d.getAmount());
            }
        }
        return sum;
    }

    public void markMissing(String reason) {
        if (reason != null && !missing.contains(reason)) {
            missing.add(reason);
        }
    }

    /**
     * 「缺依据」的统一话术，直接写进 evidence，便于飞检时解释为什么没评估
     */
    public String missingText() {
        return missing.isEmpty() ? "缺少必要依据" : String.join("；", missing);
    }
}

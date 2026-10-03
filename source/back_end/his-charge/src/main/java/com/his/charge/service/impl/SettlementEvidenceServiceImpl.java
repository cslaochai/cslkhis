package com.his.charge.service.impl;

import com.his.charge.service.SettlementEvidenceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.appoint.entity.BizAppointInfo;
import com.his.appoint.mapper.BizAppointInfoMapper;
import com.his.charge.entity.BizInsuranceSettlement;
import com.his.charge.entity.BizSettlementBill;
import com.his.charge.mapper.BizInsuranceSettlementMapper;
import com.his.charge.mapper.BizSettlementBillItemMapper;
import com.his.charge.mapper.BizSettlementBillMapper;
import com.his.charge.support.SettlementEvidence;
import com.his.common.enums.BillStatusEnum;
import com.his.common.enums.EncounterTypeEnum;
import com.his.emr.entity.BizPrescription;
import com.his.emr.entity.BizPrescriptionDetail;
import com.his.emr.mapper.BizPrescriptionDetailMapper;
import com.his.emr.mapper.BizPrescriptionMapper;
import com.his.emr.entity.BizMedicalRecord;
import com.his.emr.mapper.BizMedicalRecordMapper;
import com.his.medicaltech.entity.BizInspectionRecord;
import com.his.medicaltech.entity.BizLabResult;
import com.his.medicaltech.entity.BizLaboratoryRecord;
import com.his.medicaltech.mapper.BizInspectionRecordMapper;
import com.his.medicaltech.mapper.BizLabResultMapper;
import com.his.medicaltech.mapper.BizLaboratoryRecordMapper;
import com.his.patient.entity.BizPatient;
import com.his.patient.mapper.BizPatientMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 依证据聚合服务：把一个 regist_id 上的所有「能作为编码依据」的数据捞齐。
 *
 * <p>检索顺序刻意与清单的锚点链一致：账单由 L2 出账时写在清单上（账单ID，一张账单一张清单），
 * 账单缺失时才按挂号（门诊 encounter_id = registId）→ 患者回捞。回退是因为历史数据里
 * 清单可能没有账单，而少捞一层就意味着多一批「假通过」。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementEvidenceServiceImpl implements SettlementEvidenceService {

    private final BizAppointInfoMapper appointInfoMapper;
    private final BizPatientMapper patientMapper;
    private final BizMedicalRecordMapper medicalRecordMapper;
    private final BizPrescriptionMapper prescriptionMapper;
    private final BizPrescriptionDetailMapper prescriptionDetailMapper;
    private final BizSettlementBillMapper billMapper;
    private final BizSettlementBillItemMapper billItemMapper;
    private final BizLaboratoryRecordMapper laboratoryRecordMapper;
    private final BizLabResultMapper labResultMapper;
    private final BizInspectionRecordMapper inspectionRecordMapper;
    private final BizInsuranceSettlementMapper settlementMapper;

    private static <T> T first(List<T> list) {
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    /**
     * 聚合依据包。任何一层缺失都只记进 {@link SettlementEvidence#getMissing()}，
     * 不抛异常 —— 依据不全时规则要返回「不适用」，而不是整单审核失败。
     */
    public SettlementEvidence aggregate(BizInsuranceSettlement settlement) {
        SettlementEvidence ev = new SettlementEvidence();
        ev.setSettlement(settlement);
        if (settlement == null) {
            ev.markMissing("结算清单不存在");
            return ev;
        }

        // 1. 挂号（锚点）
        Long registId = settlement.getRegistId();
        BizAppointInfo regist = registId == null ? null : appointInfoMapper.selectById(registId);
        ev.setRegist(regist);
        if (registId == null) {
            ev.markMissing("清单未关联挂号ID，无法定位就诊依据");
        } else if (regist == null) {
            ev.markMissing("挂号记录(ID=" + registId + ")不存在");
        }

        // 2. 患者
        Long patientId = settlement.getPatientId() != null ? settlement.getPatientId()
                : (regist != null ? regist.getPatientId() : null);
        BizPatient patient = patientId == null ? null : patientMapper.selectById(patientId);
        ev.setPatient(patient);
        if (patient == null) {
            ev.markMissing("患者档案缺失，性别/年龄类规则无法判定");
        }

        // 3. 病历：优先按 registId；历史数据 registId 为空时落 patientId 最近一条
        BizMedicalRecord record = null;
        if (registId != null) {
            List<BizMedicalRecord> byRegist = medicalRecordMapper.selectList(
                    new LambdaQueryWrapper<BizMedicalRecord>()
                            .eq(BizMedicalRecord::getRegistId, registId)
                            .orderByDesc(BizMedicalRecord::getCreateTime));
            record = first(byRegist);
        }
        if (record == null && patientId != null) {
            record = first(medicalRecordMapper.selectList(new LambdaQueryWrapper<BizMedicalRecord>()
                    .eq(BizMedicalRecord::getPatientId, patientId)
                    .orderByDesc(BizMedicalRecord::getCreateTime)));
            if (record != null) {
                ev.markMissing("本次就诊无病历，已回落到患者最近一次病历，诊断一致性规则结论需人工复核");
            }
        }
        ev.setMedicalRecord(record);
        if (record == null) {
            ev.markMissing("查无病历记录");
        }

        // 4. 结算账单与账单行（三级回退：清单 bill_id → 本次就诊 → 患者最近一张）
        BizSettlementBill bill = resolveBill(settlement, regist, patientId);
        ev.setBill(bill);
        if (bill != null) {
            ev.setBillItems(billItemMapper.selectByBill(bill.getId()));
        } else {
            ev.markMissing("未找到关联结算账单");
        }

        // 5. 处方（按 registId，回落 patientId）
        List<BizPrescription> prescriptions = new ArrayList<>();
        if (registId != null) {
            prescriptions = prescriptionMapper.selectList(new LambdaQueryWrapper<BizPrescription>()
                    .eq(BizPrescription::getRegistId, registId)
                    .orderByAsc(BizPrescription::getId));
        }
        if (prescriptions.isEmpty() && patientId != null) {
            prescriptions = prescriptionMapper.selectList(new LambdaQueryWrapper<BizPrescription>()
                    .eq(BizPrescription::getPatientId, patientId)
                    .orderByDesc(BizPrescription::getCreateTime));
        }
        ev.setPrescriptions(prescriptions);
        if (!prescriptions.isEmpty()) {
            List<Long> pids = prescriptions.stream().map(BizPrescription::getId).collect(Collectors.toList());
            ev.setPrescriptionDetails(prescriptionDetailMapper.selectList(
                    new LambdaQueryWrapper<BizPrescriptionDetail>()
                            .in(BizPrescriptionDetail::getPrescriptionId, pids)
                            .orderByAsc(BizPrescriptionDetail::getId)));
        }

        // 6. 检验：表里没有 regist_id，只能按 patientId + 就诊日期定位
        LocalDate visitDate = regist != null ? regist.getVisitDate() : null;
        List<BizLaboratoryRecord> labs = selectLabs(patientId, visitDate);
        ev.setLabRecords(labs);
        if (!labs.isEmpty()) {
            List<Long> recordIds = labs.stream().map(BizLaboratoryRecord::getId).collect(Collectors.toList());
            ev.setLabResults(labResultMapper.selectList(new LambdaQueryWrapper<BizLabResult>()
                    .in(BizLabResult::getRecordId, recordIds)
                    .orderByAsc(BizLabResult::getSortOrder)));
        }
        if (!labs.isEmpty() && visitDate == null) {
            ev.markMissing("检验记录只能按患者聚合（无就诊日期），可能含其他次就诊数据");
        }

        // 7. 检查：同样按 patientId + 就诊日期
        ev.setInspections(selectInspections(patientId, visitDate));

        return ev;
    }

    /**
     * 窗口期内同一患者的其他结算清单（用于分解住院判定）。
     *
     * @param patientId 患者ID
     * @param since     起算日期
     * @param excludeId 排除当前清单
     */
    public List<BizInsuranceSettlement> recentSamePatientSettlements(Long patientId, java.time.LocalDateTime since,
                                                                     Long excludeId) {
        if (patientId == null || since == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<BizInsuranceSettlement> wrapper = new LambdaQueryWrapper<BizInsuranceSettlement>()
                .eq(BizInsuranceSettlement::getPatientId, patientId)
                .ge(BizInsuranceSettlement::getCreateTime, since);
        if (excludeId != null) {
            wrapper.ne(BizInsuranceSettlement::getId, excludeId);
        }
        return settlementMapper.selectList(wrapper.orderByDesc(BizInsuranceSettlement::getCreateTime));
    }

    private List<BizLaboratoryRecord> selectLabs(Long patientId, LocalDate visitDate) {
        if (patientId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<BizLaboratoryRecord> wrapper = new LambdaQueryWrapper<BizLaboratoryRecord>()
                .eq(BizLaboratoryRecord::getPatientId, patientId)
                .orderByDesc(BizLaboratoryRecord::getCreateTime);
        if (visitDate != null) {
            wrapper.eq(BizLaboratoryRecord::getVisitDate, visitDate);
        }
        return laboratoryRecordMapper.selectList(wrapper);
    }

    private List<BizInspectionRecord> selectInspections(Long patientId, LocalDate visitDate) {
        if (patientId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<BizInspectionRecord> wrapper = new LambdaQueryWrapper<BizInspectionRecord>()
                .eq(BizInspectionRecord::getPatientId, patientId)
                .orderByDesc(BizInspectionRecord::getCreateTime);
        if (visitDate != null) {
            wrapper.eq(BizInspectionRecord::getVisitDate, visitDate);
        }
        return inspectionRecordMapper.selectList(wrapper);
    }

    private BizSettlementBill resolveBill(BizInsuranceSettlement settlement, BizAppointInfo regist, Long patientId) {
        if (settlement.getBillId() != null) {
            BizSettlementBill bill = billMapper.selectById(settlement.getBillId());
            if (bill != null) {
                return bill;
            }
        }
        // 清单没记账单（历史数据）时按 encounter 兜底；作废账单不算依据
        Long registId = settlement.getRegistId() != null ? settlement.getRegistId()
                : (regist != null ? regist.getId() : null);
        if (registId != null) {
            BizSettlementBill bill = first(billMapper.selectList(new LambdaQueryWrapper<BizSettlementBill>()
                    .eq(BizSettlementBill::getEncounterType, EncounterTypeEnum.OUTPATIENT.getCode())
                    .eq(BizSettlementBill::getEncounterId, registId)
                    .ne(BizSettlementBill::getBillStatus, BillStatusEnum.VOIDED.getCode())
                    .orderByDesc(BizSettlementBill::getCreateTime)));
            if (bill != null) {
                return bill;
            }
        }
        if (patientId != null) {
            return first(billMapper.selectList(new LambdaQueryWrapper<BizSettlementBill>()
                    .eq(BizSettlementBill::getPatientId, patientId)
                    .ne(BizSettlementBill::getBillStatus, BillStatusEnum.VOIDED.getCode())
                    .orderByDesc(BizSettlementBill::getCreateTime)));
        }
        return null;
    }
}

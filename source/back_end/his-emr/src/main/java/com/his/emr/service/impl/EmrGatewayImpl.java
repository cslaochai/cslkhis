package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.charge.support.ChargeDeptResolver;
import com.his.charge.service.EmrGateway;
import com.his.charge.vo.MedicalRecordBrief;
import com.his.charge.vo.PrescriptionBrief;
import com.his.charge.vo.PrescriptionDetailBrief;
import com.his.emr.entity.BizInspectionApply;
import com.his.emr.entity.BizLaboratoryApply;
import com.his.emr.entity.BizMedicalRecord;
import com.his.emr.entity.BizPrescription;
import com.his.emr.entity.BizPrescriptionDetail;
import com.his.emr.mapper.BizInspectionApplyMapper;
import com.his.emr.mapper.BizLaboratoryApplyMapper;
import com.his.emr.mapper.BizMedicalRecordMapper;
import com.his.emr.mapper.BizPrescriptionDetailMapper;
import com.his.emr.mapper.BizPrescriptionMapper;
import com.his.emr.service.SourcePaidAdvanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * {@link EmrGateway} 在 his-emr 侧的实现。
 *
 * <p>写方法整体委托本域 {@link SourcePaidAdvanceService}，判定逻辑（处方能不能推进、
 * 申请单能不能撤、药有没有退）原样留在病历域，端口层一行判定都不加。
 *
 * <p>读方法只把「最近一条」的排序与限条收在域内，收费域拿到的是结果不是查询条件 ——
 * 它不该知道病历是按 create_time 倒序取第一条。
 */
@Service
@RequiredArgsConstructor
public class EmrGatewayImpl implements EmrGateway {

    private final BizMedicalRecordMapper medicalRecordMapper;
    private final BizPrescriptionMapper prescriptionMapper;
    private final BizPrescriptionDetailMapper prescriptionDetailMapper;
    private final BizInspectionApplyMapper inspectionApplyMapper;
    private final BizLaboratoryApplyMapper laboratoryApplyMapper;
    private final SourcePaidAdvanceService sourcePaidAdvanceService;

    @Override
    public MedicalRecordBrief findLatestMedicalRecordByRegist(Long registId) {
        if (registId == null) {
            return null;
        }
        return first(medicalRecordMapper.selectList(new LambdaQueryWrapper<BizMedicalRecord>()
                .eq(BizMedicalRecord::getRegistId, registId)
                .orderByDesc(BizMedicalRecord::getCreateTime)));
    }

    @Override
    public MedicalRecordBrief findLatestMedicalRecordByPatient(Long patientId) {
        if (patientId == null) {
            return null;
        }
        return first(medicalRecordMapper.selectList(new LambdaQueryWrapper<BizMedicalRecord>()
                .eq(BizMedicalRecord::getPatientId, patientId)
                .orderByDesc(BizMedicalRecord::getCreateTime)));
    }


    @Override
    public List<PrescriptionBrief> listPrescriptionsByRegist(Long registId) {
        if (registId == null) {
            return List.of();
        }
        return prescriptionMapper.selectList(new LambdaQueryWrapper<BizPrescription>()
                        .eq(BizPrescription::getRegistId, registId)
                        .orderByAsc(BizPrescription::getId)).stream()
                .map(this::toBrief)
                .toList();
    }

    @Override
    public List<PrescriptionBrief> listPrescriptionsByPatient(Long patientId) {
        if (patientId == null) {
            return List.of();
        }
        return prescriptionMapper.selectList(new LambdaQueryWrapper<BizPrescription>()
                        .eq(BizPrescription::getPatientId, patientId)
                        .orderByDesc(BizPrescription::getCreateTime)).stream()
                .map(this::toBrief)
                .toList();
    }

    @Override
    public List<PrescriptionDetailBrief> listPrescriptionDetails(List<Long> prescriptionIds) {
        if (prescriptionIds == null || prescriptionIds.isEmpty()) {
            return List.of();
        }
        return prescriptionDetailMapper.selectList(new LambdaQueryWrapper<BizPrescriptionDetail>()
                        .in(BizPrescriptionDetail::getPrescriptionId, prescriptionIds)
                        .orderByAsc(BizPrescriptionDetail::getId)).stream()
                .map(e -> {
                    PrescriptionDetailBrief brief = new PrescriptionDetailBrief();
                    brief.setId(e.getId());
                    brief.setPrescriptionId(e.getPrescriptionId());
                    brief.setDrugName(e.getDrugName());
                    return brief;
                })
                .toList();
    }

    @Override
    public boolean hasPrescriptions(Long registId, Long patientId) {
        return !listPrescriptionsByRegist(registId).isEmpty()
                || !listPrescriptionsByPatient(patientId).isEmpty();
    }

    @Override
    public ChargeDeptResolver.DeptRef findDeptByPrescriptionNo(String prescriptionNo) {
        if (isBlank(prescriptionNo)) {
            return null;
        }
        BizPrescription row = prescriptionMapper.selectOne(new LambdaQueryWrapper<BizPrescription>()
                .eq(BizPrescription::getPrescriptionNo, prescriptionNo)
                .last("LIMIT 1"));
        return row == null ? null : new ChargeDeptResolver.DeptRef(row.getDeptId(), row.getDeptName());
    }

    @Override
    public ChargeDeptResolver.DeptRef findDeptByInspectionApplyNo(String applyNo) {
        if (isBlank(applyNo)) {
            return null;
        }
        BizInspectionApply row = inspectionApplyMapper.selectOne(new LambdaQueryWrapper<BizInspectionApply>()
                .eq(BizInspectionApply::getApplyNo, applyNo)
                .last("LIMIT 1"));
        return row == null ? null : new ChargeDeptResolver.DeptRef(row.getDeptId(), row.getDeptName());
    }

    @Override
    public ChargeDeptResolver.DeptRef findDeptByLaboratoryApplyNo(String applyNo) {
        if (isBlank(applyNo)) {
            return null;
        }
        BizLaboratoryApply row = laboratoryApplyMapper.selectOne(new LambdaQueryWrapper<BizLaboratoryApply>()
                .eq(BizLaboratoryApply::getApplyNo, applyNo)
                .last("LIMIT 1"));
        return row == null ? null : new ChargeDeptResolver.DeptRef(row.getDeptId(), row.getDeptName());
    }

    @Override
    public void advancePrescriptionDetail(Long prescriptionDetailId, BigDecimal paidAmount, Integer payMethod) {
        sourcePaidAdvanceService.advancePrescriptionDetail(prescriptionDetailId, paidAmount, payMethod);
    }

    @Override
    public void revertPrescriptionDetail(Long prescriptionDetailId, String reason) {
        sourcePaidAdvanceService.revertPrescriptionDetail(prescriptionDetailId, reason);
    }

    @Override
    public void advanceLaboratoryApply(Long applyId) {
        sourcePaidAdvanceService.advanceLaboratoryApply(applyId);
    }

    @Override
    public void revertLaboratoryApply(Long applyId) {
        sourcePaidAdvanceService.revertLaboratoryApply(applyId);
    }

    @Override
    public void advanceInspectionApply(Long applyId) {
        sourcePaidAdvanceService.advanceInspectionApply(applyId);
    }

    @Override
    public void revertInspectionApply(Long applyId) {
        sourcePaidAdvanceService.revertInspectionApply(applyId);
    }

    @Override
    public void assertNoDrugPendingReturn(List<Long> prescriptionDetailIds, String scene) {
        sourcePaidAdvanceService.assertNoDrugPendingReturn(prescriptionDetailIds, scene);
    }

    private PrescriptionBrief toBrief(BizPrescription e) {
        PrescriptionBrief brief = new PrescriptionBrief();
        brief.setId(e.getId());
        brief.setPrescriptionNo(e.getPrescriptionNo());
        brief.setPrescriptionStatus(e.getPrescriptionStatus());
        return brief;
    }

    private MedicalRecordBrief toBrief(BizMedicalRecord e) {
        MedicalRecordBrief brief = new MedicalRecordBrief();
        brief.setRecordNo(e.getRecordNo());
        brief.setDiagnosisCode(e.getDiagnosisCode());
        brief.setDiagnosisName(e.getDiagnosisName());
        brief.setDiagnosis(e.getDiagnosis());
        brief.setGender(e.getGender());
        brief.setAge(e.getAge());
        brief.setChiefComplaint(e.getChiefComplaint());
        brief.setPresentIllness(e.getPresentIllness());
        brief.setPastHistory(e.getPastHistory());
        brief.setSpecialistExam(e.getSpecialistExam());
        brief.setAuxiliaryExam(e.getAuxiliaryExam());
        brief.setTreatmentPlan(e.getTreatmentPlan());
        return brief;
    }

    private MedicalRecordBrief first(List<BizMedicalRecord> list) {
        return list == null || list.isEmpty() ? null : toBrief(list.get(0));
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}

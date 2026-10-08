package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.charge.api.EmrGateway;
import com.his.charge.support.ChargeDeptResolver;
import com.his.charge.vo.MedicalRecordBriefVO;
import com.his.charge.vo.PrescriptionBriefVO;
import com.his.charge.vo.PrescriptionDetailBriefVO;
import com.his.common.util.TextUtil;
import com.his.emr.entity.*;
import com.his.emr.mapper.*;
import com.his.emr.service.SourcePaidAdvanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * EmrGateway 在 his-emr 侧的实现。
 */
@Service
@RequiredArgsConstructor
public class EmrGatewayImpl implements EmrGateway {

    private final BizMedicalRecordMapper bizMedicalRecordMapper;
    private final BizPrescriptionMapper bizPrescriptionMapper;
    private final BizPrescriptionDetailMapper bizPrescriptionDetailMapper;
    private final BizInspectionApplyMapper bizInspectionApplyMapper;
    private final BizLaboratoryApplyMapper bizLaboratoryApplyMapper;
    private final SourcePaidAdvanceService sourcePaidAdvanceService;

    @Override
    public MedicalRecordBriefVO findLatestMedicalRecordByRegist(Long registId) {
        if (registId == null) {
            return null;
        }
        return first(bizMedicalRecordMapper.selectList(new LambdaQueryWrapper<BizMedicalRecord>()
                .eq(BizMedicalRecord::getRegistId, registId)
                .orderByDesc(BizMedicalRecord::getCreateTime)));
    }

    @Override
    public MedicalRecordBriefVO findLatestMedicalRecordByPatient(Long patientId) {
        if (patientId == null) {
            return null;
        }
        return first(bizMedicalRecordMapper.selectList(new LambdaQueryWrapper<BizMedicalRecord>()
                .eq(BizMedicalRecord::getPatientId, patientId)
                .orderByDesc(BizMedicalRecord::getCreateTime)));
    }


    @Override
    public List<PrescriptionBriefVO> listPrescriptionsByRegist(Long registId) {
        if (registId == null) {
            return List.of();
        }
        return bizPrescriptionMapper.selectList(new LambdaQueryWrapper<BizPrescription>()
                        .eq(BizPrescription::getRegistId, registId)
                        .orderByAsc(BizPrescription::getId)).stream()
                .map(this::toBrief)
                .toList();
    }

    @Override
    public List<PrescriptionBriefVO> listPrescriptionsByPatient(Long patientId) {
        if (patientId == null) {
            return List.of();
        }
        return bizPrescriptionMapper.selectList(new LambdaQueryWrapper<BizPrescription>()
                        .eq(BizPrescription::getPatientId, patientId)
                        .orderByDesc(BizPrescription::getCreateTime)).stream()
                .map(this::toBrief)
                .toList();
    }

    @Override
    public List<PrescriptionDetailBriefVO> listPrescriptionDetails(List<Long> prescriptionIds) {
        if (prescriptionIds == null || prescriptionIds.isEmpty()) {
            return List.of();
        }
        return bizPrescriptionDetailMapper.selectList(new LambdaQueryWrapper<BizPrescriptionDetail>()
                        .in(BizPrescriptionDetail::getPrescriptionId, prescriptionIds)
                        .orderByAsc(BizPrescriptionDetail::getId)).stream()
                .map(e -> {
                    PrescriptionDetailBriefVO brief = new PrescriptionDetailBriefVO();
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
        if (!TextUtil.hasText(prescriptionNo)) {
            return null;
        }
        BizPrescription row = bizPrescriptionMapper.selectOne(new LambdaQueryWrapper<BizPrescription>()
                .eq(BizPrescription::getPrescriptionNo, prescriptionNo)
                .last("LIMIT 1"));
        return row == null ? null : new ChargeDeptResolver.DeptRef(row.getDeptId(), row.getDeptName());
    }

    @Override
    public ChargeDeptResolver.DeptRef findDeptByInspectionApplyNo(String applyNo) {
        if (!TextUtil.hasText(applyNo)) {
            return null;
        }
        BizInspectionApply row = bizInspectionApplyMapper.selectOne(new LambdaQueryWrapper<BizInspectionApply>()
                .eq(BizInspectionApply::getApplyNo, applyNo)
                .last("LIMIT 1"));
        return row == null ? null : new ChargeDeptResolver.DeptRef(row.getDeptId(), row.getDeptName());
    }

    @Override
    public ChargeDeptResolver.DeptRef findDeptByLaboratoryApplyNo(String applyNo) {
        if (!TextUtil.hasText(applyNo)) {
            return null;
        }
        BizLaboratoryApply row = bizLaboratoryApplyMapper.selectOne(new LambdaQueryWrapper<BizLaboratoryApply>()
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

    private PrescriptionBriefVO toBrief(BizPrescription e) {
        PrescriptionBriefVO brief = new PrescriptionBriefVO();
        brief.setId(e.getId());
        brief.setPrescriptionNo(e.getPrescriptionNo());
        brief.setPrescriptionStatus(e.getPrescriptionStatus());
        return brief;
    }

    private MedicalRecordBriefVO toBrief(BizMedicalRecord e) {
        MedicalRecordBriefVO brief = new MedicalRecordBriefVO();
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

    private MedicalRecordBriefVO first(List<BizMedicalRecord> list) {
        return list == null || list.isEmpty() ? null : toBrief(list.get(0));
    }
}

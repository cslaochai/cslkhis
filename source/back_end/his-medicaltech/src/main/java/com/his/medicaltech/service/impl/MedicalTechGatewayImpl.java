package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.charge.api.MedicalTechGateway;
import com.his.charge.vo.InspectionRecordBriefVO;
import com.his.charge.vo.LabResultBriefVO;
import com.his.charge.vo.LaboratoryRecordBriefVO;
import com.his.medicaltech.entity.BizInspectionRecord;
import com.his.medicaltech.entity.BizLabResult;
import com.his.medicaltech.entity.BizLaboratoryRecord;
import com.his.medicaltech.mapper.BizInspectionRecordMapper;
import com.his.medicaltech.mapper.BizLabResultMapper;
import com.his.medicaltech.mapper.BizLaboratoryRecordMapper;
import com.his.medicaltech.service.MedicalTechService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * MedicalTechGateway 在 his-medicaltech 侧的实现。
 */
@Service
@RequiredArgsConstructor
public class MedicalTechGatewayImpl implements MedicalTechGateway {

    private final BizLaboratoryRecordMapper bizLaboratoryRecordMapper;
    private final BizLabResultMapper bizLabResultMapper;
    private final BizInspectionRecordMapper bizInspectionRecordMapper;
    private final MedicalTechService medicalTechService;

    @Override
    public List<LaboratoryRecordBriefVO> listLaboratoryRecords(Long patientId, LocalDate visitDate) {
        if (patientId == null) {
            return List.of();
        }
        LambdaQueryWrapper<BizLaboratoryRecord> wrapper = new LambdaQueryWrapper<BizLaboratoryRecord>()
                .eq(BizLaboratoryRecord::getPatientId, patientId)
                .orderByDesc(BizLaboratoryRecord::getCreateTime);
        if (visitDate != null) {
            wrapper.eq(BizLaboratoryRecord::getVisitDate, visitDate);
        }
        return bizLaboratoryRecordMapper.selectList(wrapper).stream()
                .map(this::toBrief)
                .toList();
    }

    @Override
    public List<LabResultBriefVO> listLabResults(Long patientId, LocalDate visitDate) {
        List<Long> recordIds = listLaboratoryRecords(patientId, visitDate).stream()
                .map(LaboratoryRecordBriefVO::getId)
                .toList();
        if (recordIds.isEmpty()) {
            return List.of();
        }
        return bizLabResultMapper.selectList(new LambdaQueryWrapper<BizLabResult>()
                        .in(BizLabResult::getRecordId, recordIds)
                        .orderByAsc(BizLabResult::getSortOrder)).stream()
                .map(this::toBrief)
                .toList();
    }

    @Override
    public List<InspectionRecordBriefVO> listInspections(Long patientId, LocalDate visitDate) {
        if (patientId == null) {
            return List.of();
        }
        LambdaQueryWrapper<BizInspectionRecord> wrapper = new LambdaQueryWrapper<BizInspectionRecord>()
                .eq(BizInspectionRecord::getPatientId, patientId)
                .orderByDesc(BizInspectionRecord::getCreateTime);
        if (visitDate != null) {
            wrapper.eq(BizInspectionRecord::getVisitDate, visitDate);
        }
        return bizInspectionRecordMapper.selectList(wrapper).stream()
                .map(this::toBrief)
                .toList();
    }

    @Override
    public void ensureInspectionRecordFromApply(Long applyId) {
        medicalTechService.ensureInspectionRecordFromApply(applyId);
    }

    @Override
    public void ensureLaboratoryRecordFromApply(Long applyId) {
        medicalTechService.ensureLaboratoryRecordFromApply(applyId);
    }

    @Override
    public void cancelInspectionByApplyId(Long applyId, String reason) {
        medicalTechService.cancelInspectionByApplyId(applyId, reason);
    }

    @Override
    public void cancelLaboratoryByApplyId(Long applyId, String reason) {
        medicalTechService.cancelLaboratoryByApplyId(applyId, reason);
    }

    private LaboratoryRecordBriefVO toBrief(BizLaboratoryRecord e) {
        LaboratoryRecordBriefVO brief = new LaboratoryRecordBriefVO();
        brief.setId(e.getId());
        brief.setPatientId(e.getPatientId());
        brief.setLaboratoryItemName(e.getLaboratoryItemName());
        brief.setDiagnosis(e.getDiagnosis());
        brief.setSuggestions(e.getSuggestions());
        brief.setVisitDate(e.getVisitDate());
        return brief;
    }

    private LabResultBriefVO toBrief(BizLabResult e) {
        LabResultBriefVO brief = new LabResultBriefVO();
        brief.setId(e.getId());
        brief.setRecordId(e.getRecordId());
        brief.setLaboratoryItemName(e.getLaboratoryItemName());
        brief.setResultValue(e.getResultValue());
        brief.setResultUnit(e.getResultUnit());
        brief.setAbnormalDesc(e.getAbnormalDesc());
        brief.setReferenceRange(e.getReferenceRange());
        brief.setJudgeNote(e.getJudgeNote());
        return brief;
    }

    private InspectionRecordBriefVO toBrief(BizInspectionRecord e) {
        InspectionRecordBriefVO brief = new InspectionRecordBriefVO();
        brief.setId(e.getId());
        brief.setPatientId(e.getPatientId());
        brief.setInspectionItemName(e.getInspectionItemName());
        brief.setClinicalDiagnosis(e.getClinicalDiagnosis());
        brief.setResultConclusion(e.getResultConclusion());
        brief.setVisitDate(e.getVisitDate());
        return brief;
    }
}

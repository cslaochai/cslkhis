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
 * {@link MedicalTechGateway} 在 his-medicaltech 侧的实现。
 *
 * <p>读：把「记录 → 结果项」两段查询收在这里对外平铺 —— 父子关系是医技域的内部结构，
 * 收费域不该知道 {@code biz_lab_result.record_id} 这层关系。
 *
 * <p>写：直接委托本域 {@link MedicalTechService} 的既有实现，判定逻辑（能不能开工、
 * 能不能撤销）原样保留在医技域，不在端口层另写一套。
 *
 * <p>接口返回 void 而领域方法返回 Long/boolean 是有意的：调用方是收费域，
 * 它只关心"动作发出去了"，不消费记录ID与成败标志 —— 后者是医技域自己的事。
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

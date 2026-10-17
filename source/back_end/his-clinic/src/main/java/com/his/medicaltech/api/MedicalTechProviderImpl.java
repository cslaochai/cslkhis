package com.his.medicaltech.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.medicaltech.entity.BizCriticalValue;
import com.his.medicaltech.entity.BizInspectionRecord;
import com.his.provider.service.MedicalTechProvider;
import com.his.provider.vo.ExecStatusVO;
import com.his.provider.vo.InspectionRecordBriefVO;
import com.his.provider.vo.LabResultBriefVO;
import com.his.provider.vo.LaboratoryRecordBriefVO;
import com.his.medicaltech.entity.BizLabResult;
import com.his.medicaltech.entity.BizLaboratoryRecord;
import com.his.common.enums.InsRecordStatusEnum;
import com.his.common.enums.LabRecordStatusEnum;
import com.his.medicaltech.mapper.BizCriticalValueMapper;
import com.his.medicaltech.mapper.BizInspectionRecordMapper;
import com.his.medicaltech.mapper.BizLabResultMapper;
import com.his.medicaltech.mapper.BizLaboratoryRecordMapper;
import com.his.medicaltech.service.MedicalTechService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MedicalTechApi 在 his-medicaltech 侧的实现。
 */
@Service
@RequiredArgsConstructor
public class MedicalTechProviderImpl implements MedicalTechProvider {

    /**
     * 检查记录「已签到」起算已开始执行
     */
    private static final int INS_STARTED_FROM = InsRecordStatusEnum.SIGNED_IN.getCode();

    /**
     * 检验记录「已采样」起算已开始执行（标本都抽了，不能说没开始）
     */
    private static final int LAB_STARTED_FROM = LabRecordStatusEnum.SAMPLED.getCode();

    private final BizLaboratoryRecordMapper bizLaboratoryRecordMapper;
    private final BizLabResultMapper bizLabResultMapper;
    private final BizInspectionRecordMapper bizInspectionRecordMapper;
    private final BizCriticalValueMapper bizCriticalValueMapper;
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

    @Override
    public List<ExecStatusVO> listInspectionExecStatus(List<Long> applyIds) {
        if (CollectionUtils.isEmpty(applyIds)) {
            return Collections.emptyList();
        }
        List<BizInspectionRecord> records = bizInspectionRecordMapper.selectList(
                new LambdaQueryWrapper<BizInspectionRecord>()
                        .in(BizInspectionRecord::getApplyId, applyIds)
                        .orderByAsc(BizInspectionRecord::getId));
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        Set<Long> criticalRecordIds = criticalRecordIds(
                records.stream().map(BizInspectionRecord::getId).collect(Collectors.toList()));

        List<ExecStatusVO> result = new ArrayList<>(records.size());
        Set<Long> seenApply = new HashSet<>();
        for (BizInspectionRecord r : records) {
            // 存量数据里同一申请单可能有多条执行记录（幂等缺失留下的），
            // 这里只取最早那条作为「主线」，其余不参与展示 —— 但也不假装它们不存在，
            // 由 verification 脚本去报「同申请单多条记录」这个数据问题。
            if (!seenApply.add(r.getApplyId())) {
                continue;
            }
            ExecStatusVO s = new ExecStatusVO();
            s.setApplyId(r.getApplyId());
            s.setExecRecordId(r.getId());
            s.setExecStatus(r.getRecordStatus());
            s.setExecStatusText(inspectionText(r.getRecordStatus()));
            s.setStarted(r.getRecordStatus() != null && r.getRecordStatus() >= INS_STARTED_FROM);
            s.setCritical(criticalRecordIds.contains(r.getId()));
            result.add(s);
        }
        return result;
    }

    @Override
    public List<ExecStatusVO> listLaboratoryExecStatus(List<Long> applyIds) {
        if (CollectionUtils.isEmpty(applyIds)) {
            return Collections.emptyList();
        }
        List<BizLaboratoryRecord> records = bizLaboratoryRecordMapper.selectList(
                new LambdaQueryWrapper<BizLaboratoryRecord>()
                        .in(BizLaboratoryRecord::getApplyId, applyIds)
                        .orderByAsc(BizLaboratoryRecord::getId));
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        Set<Long> criticalRecordIds = criticalRecordIds(
                records.stream().map(BizLaboratoryRecord::getId).collect(Collectors.toList()));

        List<ExecStatusVO> result = new ArrayList<>(records.size());
        Set<Long> seenApply = new HashSet<>();
        for (BizLaboratoryRecord r : records) {
            if (!seenApply.add(r.getApplyId())) {
                continue;
            }
            ExecStatusVO s = new ExecStatusVO();
            s.setApplyId(r.getApplyId());
            s.setExecRecordId(r.getId());
            s.setExecStatus(r.getRecordStatus());
            s.setExecStatusText(laboratoryText(r.getRecordStatus()));
            s.setStarted(r.getRecordStatus() != null && r.getRecordStatus() >= LAB_STARTED_FROM);
            s.setCritical(criticalRecordIds.contains(r.getId()));
            result.add(s);
        }
        return result;
    }

    /**
     * 查这批执行记录里有哪些挂了未作废的危急值（status != 4 已作废）。
     */
    private Set<Long> criticalRecordIds(List<Long> recordIds) {
        if (CollectionUtils.isEmpty(recordIds)) {
            return Collections.emptySet();
        }
        List<BizCriticalValue> list = bizCriticalValueMapper.selectList(
                new LambdaQueryWrapper<BizCriticalValue>()
                        .in(BizCriticalValue::getRecordId, recordIds)
                        .ne(BizCriticalValue::getStatus, 4));
        return list.stream().map(BizCriticalValue::getRecordId).collect(Collectors.toSet());
    }

    /**
     * 检查记录状态 → 医生站文案。
     *
     * <p>注意 1「已登记」对医生来说不是"已登记"，而是"钱交了、还没去检查科"。
     */
    private String inspectionText(Integer status) {
        if (status == null) {
            return "已缴费待执行";
        }
        InsRecordStatusEnum e = InsRecordStatusEnum.fromCode(status);
        if (e == null) {
            return InsRecordStatusEnum.getLabelOrUnknown(status);
        }
        return switch (e) {
            case REGISTERED -> "已缴费待执行";
            case SIGNED_IN -> "已到检";
            case CHECKING -> "检查中";
            case RESULTED -> "已出结果";
            case REVIEWED -> "已审核";
            case PUBLISHED -> "已发布";
            case CANCELLED -> "已取消";
        };
    }

    /**
     * 检验记录状态 → 医生站文案。
     *
     * <p>检验的码表与检查**不是同一套**：这里 2 = 已采样、3 = 已接收，
     * 而检查的 2 = 已签到 —— 所以文案必须在医技模块算，不能由调用方按码值猜。
     */
    private String laboratoryText(Integer status) {
        if (status == null) {
            return "已缴费待执行";
        }
        LabRecordStatusEnum e = LabRecordStatusEnum.fromCode(status);
        if (e == null) {
            return LabRecordStatusEnum.getLabelOrUnknown(status);
        }
        return switch (e) {
            case REGISTERED -> "已缴费待执行";
            case SAMPLED -> "已采样";
            case RECEIVED -> "已接收";
            case TESTING -> "检测中";
            case RESULTED -> "已出结果";
            case REVIEWED -> "已审核";
            case RELEASED -> "已发布";
            case CANCELLED -> "已取消";
        };
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

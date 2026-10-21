package com.his.medicaltech.spi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.exception.BusinessException;
import com.his.medicaltech.entity.BizCriticalValue;
import com.his.medicaltech.entity.BizLabResult;
import com.his.medicaltech.entity.BizLaboratoryRecord;
import com.his.medicaltech.entity.BizReport;
import com.his.medicaltech.entity.SysLaboratoryItem;
import com.his.medicaltech.mapper.BizCriticalValueMapper;
import com.his.medicaltech.mapper.BizLabResultMapper;
import com.his.medicaltech.mapper.BizLaboratoryRecordMapper;
import com.his.medicaltech.mapper.BizReportMapper;
import com.his.medicaltech.mapper.SysLaboratoryItemMapper;
import com.his.medicaltech.support.LabAbnormalJudge;
import com.his.medicaltech.support.LabCriticalValueRules;
import com.his.medicaltech.support.LabReferenceRange;
import com.his.medicaltech.support.LabReferenceRangeParser;
import com.his.medicaltech.support.LabReferenceRangeResolver;
import com.his.spi.dto.AiLabConclusionSaveDTO;
import com.his.spi.service.AiLabDataFacadeService;
import com.his.spi.vo.AiLabRecordVO;
import com.his.spi.vo.AiLabResultRowVO;
import com.his.spi.vo.AiReportVO;
import com.his.spi.vo.JudgeResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

/**
 * AiLabDataFacadeService 在 his-medicaltech 侧的实现。
 *
 * <p>异常方向、未判定、危急值、参考区间解析全部走本模块内部的判定能力
 * （{@link LabAbnormalJudge} / {@link LabCriticalValueRules} / {@link LabReferenceRangeResolver}），
 * 判定口径全仓只有这一份，AI 模块不复制规则。</p>
 */
@Service
@RequiredArgsConstructor
public class AiLabDataFacadeServiceImpl implements AiLabDataFacadeService {

    /**
     * 危急值闭环记录「已作废」状态：已作废的不再作为危急值展示
     */
    private static final int CRITICAL_CANCELLED = 4;

    private final BizLaboratoryRecordMapper bizLaboratoryRecordMapper;
    private final BizLabResultMapper bizLabResultMapper;
    private final BizReportMapper bizReportMapper;
    private final BizCriticalValueMapper bizCriticalValueMapper;
    private final SysLaboratoryItemMapper sysLaboratoryItemMapper;
    private final LabReferenceRangeResolver labReferenceRangeResolver;

    @Override
    public AiLabRecordVO findLabRecord(Long recordId) {
        if (recordId == null) {
            return null;
        }
        BizLaboratoryRecord record = bizLaboratoryRecordMapper.selectById(recordId);
        if (record == null) {
            return null;
        }
        AiLabRecordVO vo = new AiLabRecordVO();
        vo.setId(record.getId());
        vo.setRecordNo(record.getRecordNo());
        vo.setPatientId(record.getPatientId());
        vo.setPatientName(record.getPatientName());
        vo.setGender(record.getGender());
        vo.setAge(record.getAge());
        vo.setLaboratoryItemId(record.getLaboratoryItemId());
        vo.setLaboratoryItemName(record.getLaboratoryItemName());
        vo.setExecuteTime(record.getExecuteTime());
        vo.setDiagnosis(record.getDiagnosis());
        return vo;
    }

    @Override
    public List<AiLabResultRowVO> listLabResults(Long recordId) {
        if (recordId == null) {
            return List.of();
        }
        List<BizLabResult> results = bizLabResultMapper.selectList(
                new LambdaQueryWrapper<BizLabResult>()
                        .eq(BizLabResult::getRecordId, recordId)
                        .orderByAsc(BizLabResult::getSortOrder)
                        .orderByAsc(BizLabResult::getId));
        if (CollectionUtils.isEmpty(results)) {
            return List.of();
        }
        List<BizCriticalValue> criticals = activeCriticals(recordId);
        return results.stream().map(result -> toResultRow(result, criticals)).toList();
    }

    @Override
    public List<AiLabRecordVO> listHistoryLabRecords(Long patientId, Long laboratoryItemId,
                                                     Long excludeRecordId, int limit) {
        if (patientId == null || limit <= 0) {
            return List.of();
        }
        LambdaQueryWrapper<BizLaboratoryRecord> wrapper = new LambdaQueryWrapper<BizLaboratoryRecord>()
                .eq(BizLaboratoryRecord::getPatientId, patientId)
                .eq(laboratoryItemId != null, BizLaboratoryRecord::getLaboratoryItemId, laboratoryItemId)
                .ne(excludeRecordId != null, BizLaboratoryRecord::getId, excludeRecordId)
                // 只要真正做过检测的历史：没有检测时间的记录不参与趋势比对
                .isNotNull(BizLaboratoryRecord::getExecuteTime)
                .orderByDesc(BizLaboratoryRecord::getExecuteTime)
                .last("LIMIT " + limit);
        return bizLaboratoryRecordMapper.selectList(wrapper).stream()
                .map(this::toRecordVo)
                .toList();
    }

    @Override
    public AiReportVO findReport(Long reportId) {
        if (reportId == null) {
            return null;
        }
        BizReport report = bizReportMapper.selectById(reportId);
        if (report == null) {
            return null;
        }
        AiReportVO vo = new AiReportVO();
        vo.setId(report.getId());
        vo.setReportNo(report.getReportNo());
        vo.setReportType(report.getReportType());
        vo.setReportStatus(report.getReportStatus());
        vo.setRecordId(report.getRecordId());
        vo.setPatientId(report.getPatientId());
        vo.setPatientName(report.getPatientName());
        vo.setItemName(report.getItemName());
        vo.setApplyDeptName(report.getApplyDeptName());
        vo.setApplyDoctorName(report.getApplyDoctorName());
        vo.setExamMethod(report.getExamMethod());
        vo.setPositiveFlag(report.getPositiveFlag());
        vo.setIsCritical(report.getIsCritical());
        vo.setReportContent(report.getReportContent());
        vo.setConclusion(report.getConclusion());
        vo.setSuggestions(report.getSuggestions());
        vo.setPublishTime(report.getPublishTime());
        return vo;
    }

    @Override
    public void saveConclusion(AiLabConclusionSaveDTO dto) {
        if (dto == null || dto.getRecordId() == null) {
            throw new BusinessException("检验记录ID不能为空");
        }
        BizLaboratoryRecord record = bizLaboratoryRecordMapper.selectById(dto.getRecordId());
        if (record == null) {
            throw new BusinessException("检验记录不存在：" + dto.getRecordId());
        }
        // 结论覆盖写；建议为 null 时保留记录上原有建议，不抹平
        record.setDiagnosis(dto.getConclusion());
        if (dto.getSuggestions() != null) {
            record.setSuggestions(dto.getSuggestions());
        }
        bizLaboratoryRecordMapper.updateById(record);
    }

    @Override
    public JudgeResultVO judgeValue(Long laboratoryItemId, String resultValue) {
        JudgeResultVO vo = new JudgeResultVO();
        vo.setUnjudged(Boolean.TRUE);
        vo.setCritical(Boolean.FALSE);
        vo.setNumericValue(LabReferenceRangeParser.firstNumber(resultValue));

        if (laboratoryItemId == null) {
            vo.setAbnormalFlagText("未判定");
            return vo;
        }
        SysLaboratoryItem item = sysLaboratoryItemMapper.selectById(laboratoryItemId);
        if (item == null) {
            vo.setAbnormalFlagText("未判定");
            return vo;
        }

        LabReferenceRange range = labReferenceRangeResolver.resolve(
                item.getReferenceValue(), item.getItemCode(), item.getItemName(), null);
        LabAbnormalJudge.Verdict verdict = LabAbnormalJudge.judge(resultValue, range);
        if (verdict.judged()) {
            vo.setUnjudged(Boolean.FALSE);
            vo.setAbnormalFlag(verdict.flag());
        } else {
            vo.setAbnormalFlag(null);
        }
        vo.setAbnormalFlagText(LabAbnormalJudge.getText(vo.getAbnormalFlag(), verdict.note()));

        Optional<LabCriticalValueRules.Hit> hit = LabCriticalValueRules.check(
                item.getItemCode(), item.getItemName(), item.getUnit(), resultValue);
        if (hit.isPresent()) {
            vo.setCritical(Boolean.TRUE);
            vo.setCriticalDesc(hit.get().description());
        }
        return vo;
    }

    // 内部映射

    private AiLabRecordVO toRecordVo(BizLaboratoryRecord record) {
        AiLabRecordVO vo = new AiLabRecordVO();
        vo.setId(record.getId());
        vo.setRecordNo(record.getRecordNo());
        vo.setPatientId(record.getPatientId());
        vo.setPatientName(record.getPatientName());
        vo.setGender(record.getGender());
        vo.setAge(record.getAge());
        vo.setLaboratoryItemId(record.getLaboratoryItemId());
        vo.setLaboratoryItemName(record.getLaboratoryItemName());
        vo.setExecuteTime(record.getExecuteTime());
        vo.setDiagnosis(record.getDiagnosis());
        return vo;
    }

    private AiLabResultRowVO toResultRow(BizLabResult result, List<BizCriticalValue> criticals) {
        AiLabResultRowVO row = new AiLabResultRowVO();
        row.setId(result.getId());
        row.setRecordId(result.getRecordId());
        row.setLaboratoryItemCode(result.getLaboratoryItemCode());
        row.setLaboratoryItemName(result.getLaboratoryItemName());
        row.setResultValue(result.getResultValue());
        row.setResultUnit(result.getResultUnit());
        row.setReferenceRange(result.getReferenceRange());
        row.setAbnormalFlag(result.getAbnormalFlag());
        row.setJudgeNote(result.getJudgeNote());
        row.setAbnormalFlagText(LabAbnormalJudge.getText(result.getAbnormalFlag(), result.getJudgeNote()));
        row.setUnjudged(LabAbnormalJudge.isUnjudged(result.getJudgeNote()));
        row.setNumericValue(LabReferenceRangeParser.firstNumber(result.getResultValue()));

        BizCriticalValue critical = matchCritical(result, criticals);
        if (critical != null) {
            row.setCritical(Boolean.TRUE);
            row.setCriticalDesc(critical.getCriticalDesc());
        } else {
            row.setCritical(Boolean.FALSE);
        }
        return row;
    }

    /**
     * 取该检验记录下未作废的危急值闭环记录。
     */
    private List<BizCriticalValue> activeCriticals(Long recordId) {
        return bizCriticalValueMapper.selectList(
                new LambdaQueryWrapper<BizCriticalValue>()
                        .eq(BizCriticalValue::getRecordId, recordId)
                        .ne(BizCriticalValue::getStatus, CRITICAL_CANCELLED));
    }

    /**
     * 结果行匹配危急值：编码精确优先，编码缺失时按项目名匹配。
     */
    private BizCriticalValue matchCritical(BizLabResult result, List<BizCriticalValue> criticals) {
        if (CollectionUtils.isEmpty(criticals)) {
            return null;
        }
        for (BizCriticalValue critical : criticals) {
            if (result.getLaboratoryItemCode() != null
                    && result.getLaboratoryItemCode().equals(critical.getItemCode())) {
                return critical;
            }
        }
        for (BizCriticalValue critical : criticals) {
            if (result.getLaboratoryItemName() != null
                    && result.getLaboratoryItemName().equals(critical.getItemName())) {
                return critical;
            }
        }
        return null;
    }
}

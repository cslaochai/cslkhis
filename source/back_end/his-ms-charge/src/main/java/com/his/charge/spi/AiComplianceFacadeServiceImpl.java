package com.his.charge.spi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.charge.entity.BizComplianceAudit;
import com.his.charge.entity.BizComplianceAuditItem;
import com.his.charge.entity.BizInsuranceSettlement;
import com.his.charge.mapper.BizComplianceAuditItemMapper;
import com.his.charge.mapper.BizComplianceAuditMapper;
import com.his.charge.mapper.BizInsuranceSettlementMapper;
import com.his.charge.service.SettlementEvidenceService;
import com.his.charge.support.SettlementEvidence;
import com.his.common.enums.SysGenderEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.spi.service.AiComplianceFacadeService;
import com.his.spi.vo.ComplianceAuditBriefVO;
import com.his.spi.vo.ComplianceAuditItemRowVO;
import com.his.spi.vo.ComplianceEvidenceNarrativeVO;
import com.his.spi.vo.LabResultBriefVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 收费域医保合规审核对外端口实现：只读，取审核结论、逐规则判定明细与证据叙事包，不写库、不改规则结论。
 *
 * <p>叶子适配器：只注入本模块 mapper 与纯只读依据聚合 {@link SettlementEvidenceService}（其本身仅依赖 mapper 与跨域 spi 端口，
 * 不回牵编排服务），不注入合规编排 Service。
 */
@Service
@RequiredArgsConstructor
public class AiComplianceFacadeServiceImpl implements AiComplianceFacadeService {

    /**
     * 病历叙述放宽到 1500（事实主体），项目名与检验摘要 800，与页面审核出参口径一致
     */
    private static final int NARRATIVE_MAX = 1500;

    private static final int TEXT_MAX = 800;

    /**
     * 检验摘要最多 30 条，防提示词失控
     */
    private static final int LAB_MAX = 30;

    private final BizComplianceAuditMapper bizComplianceAuditMapper;

    private final BizComplianceAuditItemMapper bizComplianceAuditItemMapper;

    private final BizInsuranceSettlementMapper bizInsuranceSettlementMapper;

    private final SettlementEvidenceService settlementEvidenceService;

    @Override
    public ComplianceAuditBriefVO getAuditDetail(Long auditId) {
        BizComplianceAudit audit = requireAudit(auditId);
        ComplianceAuditBriefVO vo = new ComplianceAuditBriefVO();
        vo.setId(audit.getId());
        vo.setAuditNo(audit.getAuditNo());
        vo.setSettlementId(audit.getSettlementId());
        BizInsuranceSettlement settlement = bizInsuranceSettlementMapper.selectById(audit.getSettlementId());
        if (settlement != null) {
            vo.setSettlementNo(settlement.getSettlementNo());
        }
        vo.setItems(loadAuditItems(auditId).stream().map(this::toRowVO).collect(Collectors.toList()));
        return vo;
    }

    @Override
    public ComplianceEvidenceNarrativeVO getAiEvidenceNarrative(Long auditId) {
        BizComplianceAudit audit = requireAudit(auditId);
        BizInsuranceSettlement settlement = bizInsuranceSettlementMapper.selectById(audit.getSettlementId());
        if (settlement == null) {
            throw new BusinessException("结算清单不存在");
        }
        SettlementEvidence evidence = settlementEvidenceService.aggregate(settlement);

        ComplianceEvidenceNarrativeVO vo = new ComplianceEvidenceNarrativeVO();
        vo.setSettlementId(settlement.getId());
        vo.setSettlementNo(settlement.getSettlementNo());
        vo.setDrgCode(settlement.getDrgCode());
        vo.setPatientTag(buildPatientTag(evidence));
        vo.setDiagnosisText(buildDiagnosisText(settlement));
        vo.setRecordNarrative(TextUtil.cut(evidence.recordNarrative(), NARRATIVE_MAX));
        vo.setOrderNames(TextUtil.cut(evidence.allOrderNames(), TEXT_MAX));
        vo.setLabSummary(buildLabSummary(evidence));
        vo.setMissingList(new ArrayList<>(evidence.getMissing()));
        return vo;
    }

    // ---------------------------------------------------------------- 内部组装

    private BizComplianceAudit requireAudit(Long auditId) {
        BizComplianceAudit audit = bizComplianceAuditMapper.selectById(auditId);
        if (audit == null) {
            throw new BusinessException("审核记录不存在");
        }
        return audit;
    }

    private List<BizComplianceAuditItem> loadAuditItems(Long auditId) {
        return bizComplianceAuditItemMapper.selectList(new LambdaQueryWrapper<BizComplianceAuditItem>()
                .eq(BizComplianceAuditItem::getAuditId, auditId)
                .orderByAsc(BizComplianceAuditItem::getRuleGroup)
                .orderByAsc(BizComplianceAuditItem::getRuleCode)
                .orderByAsc(BizComplianceAuditItem::getId));
    }

    private ComplianceAuditItemRowVO toRowVO(BizComplianceAuditItem entity) {
        ComplianceAuditItemRowVO row = new ComplianceAuditItemRowVO();
        row.setRuleCode(entity.getRuleCode());
        row.setRuleName(entity.getRuleName());
        row.setResult(entity.getResult());
        row.setEvidence(entity.getEvidence());
        return row;
    }

    /**
     * 患者标识只到「性别 + 年龄」：模型判证据够用，姓名/证件号不出模块
     */
    private String buildPatientTag(SettlementEvidence evidence) {
        Integer gender = evidence.gender();
        Integer age = evidence.age();
        SysGenderEnum g = SysGenderEnum.fromCode(gender);
        String genderText = g == null ? "性别未知" : g.getLabel();
        return age == null ? genderText : genderText + "，" + age + "岁";
    }

    private String buildDiagnosisText(BizInsuranceSettlement settlement) {
        String name = TextUtil.hasText(settlement.getDiagnosisName())
                ? settlement.getDiagnosisName() : settlement.getDiagnosis();
        if (!TextUtil.hasText(name)) {
            return "未填写";
        }
        return TextUtil.hasText(settlement.getDiagnosisCode())
                ? name + "（" + settlement.getDiagnosisCode() + "）" : name;
    }

    /**
     * 检验摘要只摆事实：项名：结果值单位（参考范围），最多 30 条防提示词失控
     */
    private String buildLabSummary(SettlementEvidence evidence) {
        List<LabResultBriefVO> results = evidence.getLabResults();
        if (CollectionUtils.isEmpty(results)) {
            return "无";
        }
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (LabResultBriefVO r : results) {
            if (count >= LAB_MAX) {
                break;
            }
            if (!TextUtil.hasText(r.getLaboratoryItemName())) {
                continue;
            }
            sb.append(r.getLaboratoryItemName()).append("：")
                    .append(TextUtil.hasText(r.getResultValue()) ? r.getResultValue() : "无结果");
            if (TextUtil.hasText(r.getResultUnit())) {
                sb.append(r.getResultUnit());
            }
            if (TextUtil.hasText(r.getReferenceRange())) {
                sb.append("（参考 ").append(r.getReferenceRange()).append("）");
            }
            sb.append("；");
            count++;
        }
        String text = sb.toString();
        return !TextUtil.hasText(text) ? "无" : TextUtil.cut(text, TEXT_MAX);
    }
}

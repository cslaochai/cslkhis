package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.charge.api.AppointGateway;
import com.his.charge.config.ComplianceProperties;
import com.his.charge.dto.*;
import com.his.charge.entity.*;
import com.his.charge.enums.AuditResultStateEnum;
import com.his.charge.enums.RuleCatalogEnum;
import com.his.charge.mapper.*;
import com.his.charge.service.ComplianceAuditService;
import com.his.charge.service.SettlementEvidenceService;
import com.his.charge.support.ComplianceRule;
import com.his.charge.support.RuleContext;
import com.his.charge.support.RuleFinding;
import com.his.charge.support.SettlementEvidence;
import com.his.charge.vo.*;
import com.his.common.base.PageResult;
import com.his.common.enums.SysGenderEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.system.entity.CurrentUser;
import com.his.system.entity.SysIcd10;
import com.his.system.mapper.SysIcd10Mapper;
import com.his.system.service.DictCacheService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 医保合规审核服务实现。
 *
 * <p>一次审核 = 聚合依据 → 跑全部规则 → 三态统计 → 落库留痕 → 回写诊断/手术的依据核对结果。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComplianceAuditServiceImpl implements ComplianceAuditService {
    private final BizInsuranceSettlementMapper settlementMapper;
    private final AppointGateway appointGateway;
    private final SysIcd10Mapper icd10Mapper;
    private final BizSettlementDiagnosisMapper diagnosisMapper;
    private final BizSettlementOperationMapper operationMapper;
    private final BizComplianceAuditMapper auditMapper;
    private final BizComplianceAuditItemMapper auditItemMapper;
    private final SysDrgGroupMapper drgGroupMapper;
    private final SettlementEvidenceService evidenceService;
    private final ComplianceProperties properties;
    /**
     * 全部规则实现，Spring 自动注入
     */
    private final List<ComplianceRule> rules;
    private DictCacheService dictCacheService;

    // 编码明细维护
    @Override
    public SettlementCodingVO getCoding(Long settlementId) {
        BizInsuranceSettlement settlement = requireSettlement(settlementId);
        SettlementCodingVO vo = new SettlementCodingVO();
        vo.setSettlementId(settlementId);
        vo.setSettlementNo(settlement.getSettlementNo());
        vo.setDiagnoses(loadDiagnoses(settlementId).stream().map(this::toDiagnosisVO)
                .collect(Collectors.toList()));
        vo.setOperations(loadOperations(settlementId).stream().map(this::toOperationVO)
                .collect(Collectors.toList()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCoding(SettlementCodingUpsertDTO dto) {
        requireSettlement(dto.getSettlementId());

        Long settlementId = dto.getSettlementId();
        // 整单覆盖：先删旧明细（@TableLogic 生效，走逻辑删除，查询侧自动过滤 del_flag=0），再插入新明细
        diagnosisMapper.delete(new LambdaQueryWrapper<BizSettlementDiagnosis>()
                .eq(BizSettlementDiagnosis::getSettlementId, settlementId));
        operationMapper.delete(new LambdaQueryWrapper<BizSettlementOperation>()
                .eq(BizSettlementOperation::getSettlementId, settlementId));

        if (!CollectionUtils.isEmpty(dto.getDiagnoses())) {
            int seq = 0;
            for (SettlementDiagnosisUpsertDTO item : dto.getDiagnoses()) {
                BizSettlementDiagnosis entity = new BizSettlementDiagnosis();
                BeanUtils.copyProperties(item, entity);
                entity.setId(null);
                entity.setSettlementId(settlementId);
                entity.setSeqNo(item.getSeqNo() == null ? ++seq : item.getSeqNo());
                if (entity.getDiagType() == null) {
                    entity.setDiagType(1);
                }
                diagnosisMapper.insert(entity);
            }
        }

        if (!CollectionUtils.isEmpty(dto.getOperations())) {
            int seq = 0;
            for (SettlementOperationUpsertDTO item : dto.getOperations()) {
                BizSettlementOperation entity = new BizSettlementOperation();
                BeanUtils.copyProperties(item, entity);
                entity.setId(null);
                entity.setSettlementId(settlementId);
                entity.setSeqNo(item.getSeqNo() == null ? ++seq : item.getSeqNo());
                if (entity.getIsMain() == null) {
                    entity.setIsMain(0);
                }
                operationMapper.insert(entity);
            }
        }
    }

    // 审核

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearCoding(Long settlementId) {
        requireSettlement(settlementId);
        diagnosisMapper.delete(new LambdaQueryWrapper<BizSettlementDiagnosis>()
                .eq(BizSettlementDiagnosis::getSettlementId, settlementId));
        operationMapper.delete(new LambdaQueryWrapper<BizSettlementOperation>()
                .eq(BizSettlementOperation::getSettlementId, settlementId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ComplianceAuditDetailVO audit(Long settlementId, Integer auditType) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        if (!properties.isEnabled()) {
            throw new BusinessException("医保合规审核功能已关闭（insurance.compliance.enabled=false）");
        }
        BizInsuranceSettlement settlement = requireSettlement(settlementId);

        // 1. 聚合依据包（以 regist_id 为锚点）
        SettlementEvidence evidence = evidenceService.aggregate(settlement);

        // 2. 加载审核对象
        List<BizSettlementDiagnosis> diagnoses = loadDiagnoses(settlementId);
        List<BizSettlementOperation> operations = loadOperations(settlementId);

        // 3. 预加载规则所需的外部数据（避免规则内查库）
        RuleContext ctx = new RuleContext();
        ctx.setSettlement(settlement);
        ctx.setEvidence(evidence);
        ctx.setDiagnoses(diagnoses);
        ctx.setOperations(operations);
        ctx.setProperties(properties);
        ctx.setEnabledIcdCodes(loadEnabledIcdCodes());
        ctx.setDrgTableReady(drgGroupMapper.selectCount(null) > 0);
        ctx.setDrgGroup(loadDrgGroup(settlement.getDrgCode()));
        ctx.setRecentSettlements(loadRecentSettlements(settlement));

        // 4. 跑规则
        List<RuleFinding> findings = new ArrayList<>();
        for (ComplianceRule rule : rules) {
            List<RuleFinding> result = rule.evaluate(ctx);
            if (CollectionUtils.isEmpty(result)) {
                // 规则没返回任何结论本身是个缺陷，必须暴露而不是静默跳过
                log.warn("合规规则未返回任何判定：group={}", rule.group());
                continue;
            }
            findings.addAll(result);
        }

        // 5. 三态统计
        Map<RuleCatalogEnum, AuditResultStateEnum> ruleStates = mergeByRule(findings);
        int hitCount = 0;
        int passCount = 0;
        int naCount = 0;
        int riskScore = 0;
        int maxHitRisk = 0;
        for (Map.Entry<RuleCatalogEnum, AuditResultStateEnum> e : ruleStates.entrySet()) {
            switch (e.getValue()) {
                case HIT:
                    hitCount++;
                    riskScore += RuleCatalogEnum.scoreOf(e.getKey().getRisk());
                    maxHitRisk = Math.max(maxHitRisk, e.getKey().getRisk());
                    break;
                case PASS:
                    passCount++;
                    break;
                default:
                    naCount++;
                    break;
            }
        }

        // 6. 落库
        BizComplianceAudit audit = new BizComplianceAudit();
        audit.setAuditNo(nextAuditNo());
        audit.setSettlementId(settlementId);
        audit.setRegistId(settlement.getRegistId());
        audit.setAuditType(auditType == null ? 1 : auditType);
        audit.setRiskLevel(maxHitRisk);
        audit.setRiskScore(riskScore);
        audit.setHitCount(hitCount);
        audit.setPassCount(passCount);
        audit.setNaCount(naCount);
        audit.setActualCost(evidence.actualCost());
        audit.setConclusion(buildConclusion(hitCount, naCount, riskScore, maxHitRisk));

        // DRG 字段：只有分组表真的接了才写，否则留空并说明
        SysDrgGroup group = ctx.getDrgGroup();
        if (ctx.isDrgTableReady() && group != null) {
            audit.setDrgCode(group.getDrgCode());
            audit.setDrgWeight(group.getWeight());
            audit.setPayStandard(group.getPayStandard());
            if (group.getPayStandard() != null && group.getPayStandard().signum() > 0) {
                audit.setCostRatio(evidence.actualCost()
                        .divide(group.getPayStandard(), 4, java.math.RoundingMode.HALF_UP));
            }
        } else {
            audit.setRemark(ctx.isDrgTableReady()
                    ? "清单未匹配到DRG分组，未计算费用倍率"
                    : "未接入DRG分组方案（sys_drg_group为空），未计算入组与费用倍率");
        }

        audit.setAuditBy(operatorUser.getRealName());
        audit.setAuditTime(LocalDateTime.now());
        auditMapper.insert(audit);

        for (RuleFinding f : findings) {
            BizComplianceAuditItem item = new BizComplianceAuditItem();
            item.setAuditId(audit.getId());
            item.setRuleCode(f.getRule().getCode());
            item.setRuleName(f.getRule().getName());
            item.setRuleGroup(f.getRule().getGroup());
            item.setResult(f.getResult().getCode());
            item.setRiskLevel(f.getRule().getRisk());
            item.setTargetType(f.getTargetType() == null ? f.getRule().getTargetType() : f.getTargetType());
            item.setTargetId(f.getTargetId());
            item.setTargetCode(f.getTargetCode());
            item.setTargetName(f.getTargetName());
            item.setEvidence(truncate(f.getEvidence(), 1000));
            item.setSuggestion(truncate(
                    StringUtils.hasText(f.getSuggestion()) ? f.getSuggestion() : f.getRule().getSuggestion(), 500));
            auditItemMapper.insert(item);
        }

        // 7. 回写诊断/手术的依据核对结果（三态），供清单编辑页直接看到
        writeBackEvidenceStatus(findings, diagnoses, operations);

        // 8. 组详情出参
        ComplianceAuditDetailVO vo = new ComplianceAuditDetailVO();
        BeanUtils.copyProperties(audit, vo);
        fillAuditText(vo);
        vo.setItems(loadAuditItems(audit.getId()).stream().map(this::toItemVO).collect(Collectors.toList()));
        vo.setHitItemCount((int) vo.getItems().stream()
                .filter(i -> i.getResult() != null && i.getResult() == 1).count());
        vo.setDiagnoses(loadDiagnoses(settlementId).stream().map(this::toDiagnosisVO)
                .collect(Collectors.toList()));
        vo.setOperations(loadOperations(settlementId).stream().map(this::toOperationVO)
                .collect(Collectors.toList()));
        return vo;
    }

    @Override
    public List<ComplianceAuditDetailVO> batchAudit(ComplianceBatchAuditDTO dto) {
        Set<Long> unique = new HashSet<>(dto.getSettlementIds());
        List<ComplianceAuditDetailVO> result = new ArrayList<>();
        Integer type = dto.getAuditType() == null ? 2 : dto.getAuditType();
        for (Long id : unique) {
            if (id == null) {
                continue;
            }
            try {
                result.add(audit(id, type));
            } catch (BusinessException e) {
                // 单张失败不中断整批，但必须留下痕迹，不能静默跳过
                log.warn("批量合规筛查跳过结算清单 {}：{}", id, e.getMessage());
                ComplianceAuditDetailVO failed = new ComplianceAuditDetailVO();
                failed.setSettlementId(id);
                failed.setConclusion("审核失败：" + e.getMessage());
                result.add(failed);
            }
        }
        return result;
    }

    @Override
    public PageResult<ComplianceAuditVO> selectAuditPage(ComplianceAuditQueryPageDTO queryDTO) {
        LambdaQueryWrapper<BizComplianceAudit> wrapper = new LambdaQueryWrapper<BizComplianceAudit>()
                .eq(queryDTO.getSettlementId() != null, BizComplianceAudit::getSettlementId,
                        queryDTO.getSettlementId())
                .eq(queryDTO.getRegistId() != null, BizComplianceAudit::getRegistId, queryDTO.getRegistId())
                .eq(queryDTO.getRiskLevel() != null, BizComplianceAudit::getRiskLevel, queryDTO.getRiskLevel())
                .eq(queryDTO.getAuditType() != null, BizComplianceAudit::getAuditType, queryDTO.getAuditType())
                .orderByDesc(BizComplianceAudit::getCreateTime);

        Page<BizComplianceAudit> page = auditMapper.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);

        List<ComplianceAuditVO> voList = page.getRecords().stream().map(entity -> {
            ComplianceAuditVO vo = new ComplianceAuditVO();
            BeanUtils.copyProperties(entity, vo);
            fillAuditText(vo);
            BizInsuranceSettlement settlement = settlementMapper.selectById(entity.getSettlementId());
            if (settlement != null) {
                vo.setSettlementNo(settlement.getSettlementNo());
                if (!StringUtils.hasText(vo.getDrgCode())) {
                    vo.setDrgCode(settlement.getDrgCode());
                }
            }
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public ComplianceAuditDetailVO getAuditDetail(Long auditId) {
        BizComplianceAudit audit = auditMapper.selectById(auditId);
        if (audit == null) {
            throw new BusinessException("审核记录不存在");
        }
        ComplianceAuditDetailVO vo = new ComplianceAuditDetailVO();
        BeanUtils.copyProperties(audit, vo);
        fillAuditText(vo);
        List<ComplianceAuditItemVO> items = loadAuditItems(auditId).stream().map(this::toItemVO)
                .collect(Collectors.toList());
        vo.setItems(items);
        vo.setHitItemCount((int) items.stream().filter(i -> i.getResult() != null && i.getResult() == 1).count());
        BizInsuranceSettlement settlement = settlementMapper.selectById(audit.getSettlementId());
        if (settlement != null) {
            vo.setSettlementNo(settlement.getSettlementNo());
        }
        vo.setDiagnoses(loadDiagnoses(audit.getSettlementId()).stream().map(this::toDiagnosisVO)
                .collect(Collectors.toList()));
        vo.setOperations(loadOperations(audit.getSettlementId()).stream().map(this::toOperationVO)
                .collect(Collectors.toList()));
        return vo;
    }

    @Override
    public ComplianceEvidenceNarrativeVO getAiEvidenceNarrative(Long auditId) {
        BizComplianceAudit audit = auditMapper.selectById(auditId);
        if (audit == null) {
            throw new BusinessException("审核记录不存在");
        }
        BizInsuranceSettlement settlement = requireSettlement(audit.getSettlementId());
        SettlementEvidence evidence = evidenceService.aggregate(settlement);

        ComplianceEvidenceNarrativeVO vo = new ComplianceEvidenceNarrativeVO();
        vo.setSettlementId(settlement.getId());
        vo.setSettlementNo(settlement.getSettlementNo());
        vo.setDrgCode(settlement.getDrgCode());
        vo.setPatientTag(buildPatientTag(evidence));
        vo.setDiagnosisText(buildDiagnosisText(settlement));
        // 送模型前截断：病历叙述是事实主体放宽到 1500，项目名与检验摘要 800，
        // 提示词总长可控，超长部分不是判定命中规则的关键依据
        vo.setRecordNarrative(truncate(evidence.recordNarrative(), 1500));
        vo.setOrderNames(truncate(evidence.allOrderNames(), 800));
        vo.setLabSummary(buildLabSummary(evidence));
        vo.setMissingList(new ArrayList<>(evidence.getMissing()));
        return vo;
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
        String name = StringUtils.hasText(settlement.getDiagnosisName())
                ? settlement.getDiagnosisName() : settlement.getDiagnosis();
        if (!StringUtils.hasText(name)) {
            return "未填写";
        }
        return StringUtils.hasText(settlement.getDiagnosisCode())
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
            if (count >= 30) {
                break;
            }
            if (!StringUtils.hasText(r.getLaboratoryItemName())) {
                continue;
            }
            sb.append(r.getLaboratoryItemName()).append("：")
                    .append(StringUtils.hasText(r.getResultValue()) ? r.getResultValue() : "无结果");
            if (StringUtils.hasText(r.getResultUnit())) {
                sb.append(r.getResultUnit());
            }
            if (StringUtils.hasText(r.getReferenceRange())) {
                sb.append("（参考 ").append(r.getReferenceRange()).append("）");
            }
            sb.append("；");
            count++;
        }
        String text = sb.toString();
        return !StringUtils.hasText(text) ? "无" : truncate(text, 800);
    }

    // 内部方法

    /**
     * 同一规则的多个判定合并成规则级结论：HIT > PASS > 不适用
     */
    private Map<RuleCatalogEnum, AuditResultStateEnum> mergeByRule(List<RuleFinding> findings) {
        Map<RuleCatalogEnum, AuditResultStateEnum> states = new LinkedHashMap<>();
        for (RuleFinding f : findings) {
            AuditResultStateEnum current = states.get(f.getRule());
            if (current == null || rank(f.getResult()) > rank(current)) {
                states.put(f.getRule(), f.getResult());
            }
        }
        return states;
    }

    private int rank(AuditResultStateEnum state) {
        switch (state) {
            case HIT:
                return 3;
            case PASS:
                return 2;
            default:
                return 1;
        }
    }

    /**
     * 把规则结论回写到诊断/手术明细的 evidence_status / evidence_note
     */
    private void writeBackEvidenceStatus(List<RuleFinding> findings,
                                         List<BizSettlementDiagnosis> diagnoses,
                                         List<BizSettlementOperation> operations) {
        Map<Long, List<RuleFinding>> diagFindings = new HashMap<>();
        Map<Long, List<RuleFinding>> operFindings = new HashMap<>();
        for (RuleFinding f : findings) {
            if (f.getTargetId() == null || f.getTargetType() == null) {
                continue;
            }
            if (f.getTargetType() == 1) {
                diagFindings.computeIfAbsent(f.getTargetId(), k -> new ArrayList<>()).add(f);
            } else if (f.getTargetType() == 2) {
                operFindings.computeIfAbsent(f.getTargetId(), k -> new ArrayList<>()).add(f);
            }
        }

        for (BizSettlementDiagnosis d : diagnoses) {
            List<RuleFinding> list = diagFindings.get(d.getId());
            BizSettlementDiagnosis update = new BizSettlementDiagnosis();
            update.setId(d.getId());
            if (CollectionUtils.isEmpty(list)) {
                // 一条规则都没落到这条诊断上 —— 也是「未评估」，不能留空被读成正常
                update.setEvidenceStatus(AuditResultStateEnum.NOT_APPLICABLE.getCode());
                update.setEvidenceNote("无规则覆盖该诊断，未评估");
            } else {
                update.setEvidenceStatus(worst(list).getCode());
                update.setEvidenceNote(truncate(buildNote(list), 500));
            }
            diagnosisMapper.updateById(update);
        }

        for (BizSettlementOperation o : operations) {
            List<RuleFinding> list = operFindings.get(o.getId());
            BizSettlementOperation update = new BizSettlementOperation();
            update.setId(o.getId());
            if (CollectionUtils.isEmpty(list)) {
                update.setEvidenceStatus(AuditResultStateEnum.NOT_APPLICABLE.getCode());
                update.setEvidenceNote("无规则覆盖该手术操作，未评估");
            } else {
                update.setEvidenceStatus(worst(list).getCode());
                update.setEvidenceNote(truncate(buildNote(list), 500));
            }
            operationMapper.updateById(update);
        }
    }

    private AuditResultStateEnum worst(List<RuleFinding> list) {
        AuditResultStateEnum worst = AuditResultStateEnum.NOT_APPLICABLE;
        for (RuleFinding f : list) {
            if (rank(f.getResult()) > rank(worst)) {
                worst = f.getResult();
            }
        }
        return worst;
    }

    /**
     * 依据说明：命中的优先写，没有命中就写全部结论，保证「不适用」的原因一定落盘
     */
    private String buildNote(List<RuleFinding> list) {
        List<RuleFinding> hits = list.stream()
                .filter(f -> f.getResult() == AuditResultStateEnum.HIT)
                .collect(Collectors.toList());
        List<RuleFinding> source = hits.isEmpty() ? list : hits;
        return source.stream()
                .sorted(Comparator.comparing(f -> f.getRule().getCode()))
                .map(f -> "[" + f.getRule().getCode() + " " + f.getResult().getLabel() + "] " + f.getEvidence())
                .collect(Collectors.joining("；"));
    }

    private String buildConclusion(int hitCount, int naCount, int riskScore, int maxHitRisk) {
        if (hitCount == 0 && naCount == 0) {
            return "全部规则通过，未发现高编高套或低编入组风险。";
        }
        if (hitCount == 0) {
            return "未发现违规，但有 " + naCount + " 条规则因缺少依据未能评估（见「不适用」明细），结论不完整，请补齐数据后复查。";
        }
        String level = RuleCatalogEnum.riskLabel(maxHitRisk);
        String tail = naCount == 0 ? "" : "；另有 " + naCount + " 条规则未评估，结论不完整。";
        return "命中 " + hitCount + " 条风险规则，最高风险等级「" + level + "」，风险分 " + riskScore + tail;
    }

    private void fillAuditText(ComplianceAuditVO vo) {
        vo.setRiskLevelText(RuleCatalogEnum.riskLabel(vo.getRiskLevel()));
        vo.setAuditTypeText(dictCacheService.getDicDataLabel("biz_charge_complianceAuditTypeEnum", vo.getAuditType()));
    }

    private Set<String> loadEnabledIcdCodes() {
        List<SysIcd10> list = icd10Mapper.selectList(new LambdaQueryWrapper<SysIcd10>()
                .eq(SysIcd10::getStatus, 1));
        Set<String> codes = new HashSet<>();
        for (SysIcd10 icd : list) {
            if (StringUtils.hasText(icd.getIcdCode())) {
                codes.add(icd.getIcdCode().toUpperCase());
            }
        }
        return codes;
    }

    private SysDrgGroup loadDrgGroup(String drgCode) {
        if (!StringUtils.hasText(drgCode)) {
            return null;
        }
        List<SysDrgGroup> list = drgGroupMapper.selectList(new LambdaQueryWrapper<SysDrgGroup>()
                .eq(SysDrgGroup::getDrgCode, drgCode)
                .eq(SysDrgGroup::getStatus, 1));
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    private List<BizInsuranceSettlement> loadRecentSettlements(BizInsuranceSettlement settlement) {
        if (settlement.getPatientId() == null) {
            return new ArrayList<>();
        }
        int window = properties.getReadmitWindowDays();
        LocalDateTime baseline = null;
        if (settlement.getRegistId() != null) {
            RegistBriefVO regist = appointGateway.findRegist(settlement.getRegistId());
            if (regist != null && regist.getVisitDate() != null) {
                baseline = regist.getVisitDate().atStartOfDay();
            }
        }
        if (baseline == null) {
            baseline = settlement.getCreateTime() == null ? LocalDateTime.now() : settlement.getCreateTime();
        }
        return evidenceService.recentSamePatientSettlements(
                settlement.getPatientId(), baseline.minusDays(window), settlement.getId());
    }

    private List<BizSettlementDiagnosis> loadDiagnoses(Long settlementId) {
        return diagnosisMapper.selectList(new LambdaQueryWrapper<BizSettlementDiagnosis>()
                .eq(BizSettlementDiagnosis::getSettlementId, settlementId)
                .orderByAsc(BizSettlementDiagnosis::getDiagType)
                .orderByAsc(BizSettlementDiagnosis::getSeqNo)
                .orderByAsc(BizSettlementDiagnosis::getId));
    }

    private List<BizSettlementOperation> loadOperations(Long settlementId) {
        return operationMapper.selectList(new LambdaQueryWrapper<BizSettlementOperation>()
                .eq(BizSettlementOperation::getSettlementId, settlementId)
                .orderByDesc(BizSettlementOperation::getIsMain)
                .orderByAsc(BizSettlementOperation::getSeqNo)
                .orderByAsc(BizSettlementOperation::getId));
    }

    private List<BizComplianceAuditItem> loadAuditItems(Long auditId) {
        return auditItemMapper.selectList(new LambdaQueryWrapper<BizComplianceAuditItem>()
                .eq(BizComplianceAuditItem::getAuditId, auditId)
                .orderByAsc(BizComplianceAuditItem::getRuleGroup)
                .orderByAsc(BizComplianceAuditItem::getRuleCode)
                .orderByAsc(BizComplianceAuditItem::getId));
    }

    private BizInsuranceSettlement requireSettlement(Long settlementId) {
        // C 类保留：私有兜底被多个入口共用（含 @RequestParam 与非 web 调用），Bean Validation 覆盖不到这一层
        if (settlementId == null) {
            throw new BusinessException("结算清单ID不能为空");
        }
        BizInsuranceSettlement settlement = settlementMapper.selectById(settlementId);
        if (settlement == null) {
            throw new BusinessException("结算清单不存在");
        }
        return settlement;
    }

    private String nextAuditNo() {
        return "CA" + LocalDateTime.now().format(DateFormats.COMPACT_DATETIME)
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

    private SettlementDiagnosisVO toDiagnosisVO(BizSettlementDiagnosis entity) {
        SettlementDiagnosisVO vo = new SettlementDiagnosisVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setDiagTypeText(entity.getDiagType() == null ? ""
                : (entity.getDiagType() == 1 ? "主要诊断" : "其他诊断"));
        vo.setAdmitConditionText(dictCacheService.getDicDataLabel("biz_common_admitConditionEnum", entity.getAdmitCondition()));
        // 三态中文一律走 getText，禁止在这里拼「通过」
        vo.setEvidenceStatusText(AuditResultStateEnum.getText(entity.getEvidenceStatus()));
        return vo;
    }

    private SettlementOperationVO toOperationVO(BizSettlementOperation entity) {
        SettlementOperationVO vo = new SettlementOperationVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setEvidenceStatusText(AuditResultStateEnum.getText(entity.getEvidenceStatus()));
        return vo;
    }

    private ComplianceAuditItemVO toItemVO(BizComplianceAuditItem entity) {
        ComplianceAuditItemVO vo = new ComplianceAuditItemVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setRuleGroupText(RuleCatalogEnum.groupLabel(entity.getRuleGroup()));
        vo.setResultText(AuditResultStateEnum.getText(entity.getResult()));
        vo.setRiskLevelText(RuleCatalogEnum.riskLabel(entity.getRiskLevel()));
        return vo;
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return null;
        }
        return text.length() <= max ? text : text.substring(0, max);
    }
}
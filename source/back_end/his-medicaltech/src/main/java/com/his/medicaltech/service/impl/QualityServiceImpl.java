package com.his.medicaltech.service.impl;

import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import com.his.medicaltech.dto.QualityIssueQueryPageDTO;
import com.his.medicaltech.enums.QualityDimension;
import com.his.medicaltech.enums.QualityRule;
import com.his.medicaltech.enums.QualitySeverity;
import com.his.medicaltech.mapper.QualityMapper;
import com.his.medicaltech.service.QualityService;
import com.his.medicaltech.vo.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 数据质量报表实现（P5.3）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QualityServiceImpl implements QualityService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    /**
     * 单条规则明细的安全上限：超出说明数据已严重跑偏，先截断保证接口可用并在日志里告警
     */
    private static final int MAX_ROWS_PER_RULE = 2000;
    private final QualityMapper qualityMapper;
    /**
     * 规则 → 明细查询。启动时构建并自检
     */
    private Map<QualityRule, Supplier<List<QualityIssueVO>>> registry;

    private static boolean contains(String src, String k) {
        return src != null && src.contains(k);
    }

    // 对外接口

    @PostConstruct
    void initRegistry() {
        Map<QualityRule, Supplier<List<QualityIssueVO>>> m = new EnumMap<>(QualityRule.class);
        m.put(QualityRule.PT_IDENTITY_MISS, qualityMapper::rulePtIdentityMiss);
        m.put(QualityRule.IPR_ADMIT_DOC_MISS, qualityMapper::ruleIprAdmitDocMiss);
        m.put(QualityRule.ADM_DISCHARGE_DOC_MISS, qualityMapper::ruleAdmDischargeDocMiss);
        m.put(QualityRule.RX_DETAIL_MISS, qualityMapper::ruleRxDetailMiss);
        m.put(QualityRule.LAB_APPLY_REC_MISS, qualityMapper::ruleLabApplyRecMiss);
        m.put(QualityRule.MR_DIAG_CODE_MISS, qualityMapper::ruleMrDiagCodeMiss);
        m.put(QualityRule.CHARGE_DISCOUNT_ALLOC_MISS, qualityMapper::ruleChargeDiscountAllocMiss);
        m.put(QualityRule.ALLERGY_DUAL_MISS, qualityMapper::ruleAllergyDualMiss);
        m.put(QualityRule.CHARGE_SUM_MISMATCH, qualityMapper::ruleChargeSumMismatch);
        m.put(QualityRule.CRITICAL_OVERDUE_HANDLE, qualityMapper::ruleCriticalOverdueHandle);
        m.put(QualityRule.CRITICAL_NOTIFY_MISS, qualityMapper::ruleCriticalNotifyMiss);
        m.put(QualityRule.ADM_DOC_LATE, qualityMapper::ruleAdmDocLate);
        m.put(QualityRule.DISCHARGE_ARCHIVE_LATE, qualityMapper::ruleDischargeArchiveLate);
        m.put(QualityRule.LAB_AUDIT_LATE, qualityMapper::ruleLabAuditLate);
        m.put(QualityRule.IPR_KEY_DOC_DUP, qualityMapper::ruleIprKeyDocDup);
        m.put(QualityRule.LAB_RESULT_DUP, qualityMapper::ruleLabResultDup);
        m.put(QualityRule.PT_IDCARD_DUP, qualityMapper::rulePtIdcardDup);
        m.put(QualityRule.REGIST_DUP_SAME_DAY, qualityMapper::ruleRegistDupSameDay);
        m.put(QualityRule.GENDER_IDCARD_CONFLICT, qualityMapper::ruleGenderIdcardConflict);
        m.put(QualityRule.IDCARD_FORMAT_INVALID, qualityMapper::ruleIdcardFormatInvalid);
        m.put(QualityRule.CODE_VALUE_INVALID, qualityMapper::ruleCodeValueInvalid);
        m.put(QualityRule.DATE_REVERSE, qualityMapper::ruleDateReverse);
        m.put(QualityRule.AMOUNT_INVALID, qualityMapper::ruleAmountInvalid);

        if (m.size() != QualityRule.values().length) {
            List<String> missing = Arrays.stream(QualityRule.values())
                    .filter(r -> !m.containsKey(r))
                    .map(QualityRule::getCode)
                    .toList();
            throw new IllegalStateException("数据质量规则注册不完整：枚举 " + QualityRule.values().length
                    + " 条，实现 " + m.size() + " 条，缺少 " + missing);
        }
        this.registry = Collections.unmodifiableMap(m);
        log.info("数据质量规则注册完成，共 {} 条", m.size());
    }

    @Override
    public QualitySummaryVO getSummary() {
        Map<String, QualityRuleTotalVO> totals = loadTotals();
        List<QualityRuleVO> allRules = ruleVOs(totals);

        QualitySummaryVO summary = new QualitySummaryVO();
        summary.setGeneratedAt(LocalDateTime.now().format(DateFormats.DATETIME));
        summary.setRuleCount(allRules.size());
        summary.setCheckedTotal(sum(allRules, QualityRuleVO::getCheckedTotal));
        summary.setIssueCount(sum(allRules, QualityRuleVO::getIssueCount));
        summary.setPassRate(rate(summary.getCheckedTotal(), summary.getIssueCount()));
        summary.setCleanRuleCount((int) allRules.stream().filter(r -> !Boolean.TRUE.equals(r.getHasIssue())).count());
        summary.setDirtyRuleCount((int) allRules.stream().filter(r -> Boolean.TRUE.equals(r.getHasIssue())).count());
        summary.setEmptyRuleCount((int) allRules.stream().filter(r -> Boolean.TRUE.equals(r.getEmpty())).count());
        summary.setHighIssueCount(allRules.stream()
                .filter(r -> r.getSeverity() != null && r.getSeverity() == QualitySeverity.HIGH.getLevel())
                .mapToLong(r -> r.getIssueCount() == null ? 0 : r.getIssueCount())
                .sum());

        List<QualityDimensionVO> dims = new ArrayList<>();
        for (QualityDimension d : QualityDimension.values()) {
            List<QualityRuleVO> rules = allRules.stream()
                    .filter(r -> d.name().equals(r.getDimension()))
                    .collect(Collectors.toList());
            QualityDimensionVO dv = new QualityDimensionVO();
            dv.setDimension(d.name());
            dv.setDimensionText(d.getText());
            dv.setDescription(d.getDescription());
            dv.setRuleCount(rules.size());
            dv.setRules(rules);
            long checked = sum(rules, QualityRuleVO::getCheckedTotal);
            long issues = sum(rules, QualityRuleVO::getIssueCount);
            dv.setCheckedTotal(checked);
            dv.setIssueCount(issues);
            dv.setPassRate(rate(checked, issues));
            dv.setDirtyRuleCount((int) rules.stream().filter(r -> Boolean.TRUE.equals(r.getHasIssue())).count());
            dims.add(dv);
        }
        summary.setDimensions(dims);

        summary.setTopRules(allRules.stream()
                .filter(r -> r.getIssueCount() != null && r.getIssueCount() > 0)
                .sorted(Comparator.comparingLong((QualityRuleVO r) -> r.getIssueCount()).reversed())
                .limit(5)
                .collect(Collectors.toList()));
        return summary;
    }

    @Override
    public List<QualityRuleVO> getRuleList(String dimension) {
        if (TextUtil.hasText(dimension) && QualityDimension.parse(dimension) == null) {
            throw new BusinessException("未知的数据质量维度：" + dimension);
        }
        Map<String, QualityRuleTotalVO> totals = loadTotals();
        List<QualityRuleVO> list = ruleVOs(totals);
        if (!TextUtil.hasText(dimension)) {
            return list;
        }
        QualityDimension d = QualityDimension.parse(dimension);
        return list.stream().filter(r -> d.name().equals(r.getDimension())).collect(Collectors.toList());
    }

    @Override
    public List<QualityDimensionSelectListVO> dimensionDict() {
        List<QualityDimensionSelectListVO> list = new ArrayList<>();
        for (QualityDimension d : QualityDimension.values()) {
            QualityDimensionSelectListVO item = new QualityDimensionSelectListVO();
            item.setCode(d.name());
            item.setText(d.getText());
            item.setDescription(d.getDescription());
            item.setRuleCount((int) Arrays.stream(QualityRule.values()).filter(r -> r.getDimension() == d).count());
            list.add(item);
        }
        return list;
    }

    // 内部

    @Override
    public PageResult<QualityIssueVO> listIssuePage(QualityIssueQueryPageDTO dto) {
        QualityRule only = null;
        QualityDimension dim = null;
        if (TextUtil.hasText(dto.getRuleCode())) {
            only = QualityRule.parse(dto.getRuleCode());
            if (only == null) {
                throw new BusinessException("未知的数据质量规则：" + dto.getRuleCode());
            }
        }
        if (TextUtil.hasText(dto.getDimension())) {
            dim = QualityDimension.parse(dto.getDimension());
            if (dim == null) {
                throw new BusinessException("未知的数据质量维度：" + dto.getDimension());
            }
        }

        final QualityDimension dimFilter = dim;
        List<QualityRule> targets;
        if (only != null) {
            targets = List.of(only);
        } else if (dimFilter != null) {
            targets = Arrays.stream(QualityRule.values()).filter(r -> r.getDimension() == dimFilter).toList();
        } else {
            targets = Arrays.asList(QualityRule.values());
        }

        Map<String, QualityRuleTotalVO> totals = loadTotals();
        List<QualityIssueVO> all = new ArrayList<>();
        for (QualityRule rule : targets) {
            List<QualityIssueVO> rows = loadIssues(rule);
            long checked = checkedOf(totals, rule.getCode());
            for (QualityIssueVO v : rows) {
                decorate(v, rule, checked);
                all.add(v);
            }
        }

        // 严重度 / 关键字过滤
        List<QualityIssueVO> filtered = all.stream()
                .filter(v -> dto.getSeverity() == null || dto.getSeverity().equals(v.getSeverity()))
                .filter(v -> matchKeyword(v, dto.getKeyword()))
                .sorted(Comparator.comparingInt((QualityIssueVO v) -> v.getSeverity() == null ? 0 : -v.getSeverity())
                        .thenComparing(QualityIssueVO::getRuleCode)
                        .thenComparing(v -> v.getOccurredTime() == null ? "" : v.getOccurredTime(), Comparator.reverseOrder()))
                .collect(Collectors.toList());

        int pageNum = dto.getPageNum();
        int pageSize = dto.getPageSize();
        int from = Math.min((pageNum - 1) * pageSize, filtered.size());
        int to = Math.min(from + pageSize, filtered.size());
        long pages = (filtered.size() + pageSize - 1) / pageSize;
        return PageResult.of(filtered.size(), pageNum, pageSize, pages, filtered.subList(from, to));
    }

    /**
     * 取全部分母 / 命中，并做双向自检（少了规则、多了未定义规则都要炸）
     */
    private Map<String, QualityRuleTotalVO> loadTotals() {
        List<QualityRuleTotalVO> rows = qualityMapper.selectRuleTotals();
        Map<String, QualityRuleTotalVO> map = new HashMap<>();
        for (QualityRuleTotalVO t : rows) {
            map.put(t.getRuleCode(), t);
        }
        for (QualityRule r : QualityRule.values()) {
            if (!map.containsKey(r.getCode())) {
                throw new BusinessException("规则 " + r.getCode() + " 未返回分母，报表数字不可信，已中止");
            }
        }
        for (String code : map.keySet()) {
            if (QualityRule.parse(code) == null) {
                throw new BusinessException("分母查询返回了未定义的规则码：" + code);
            }
        }
        return map;
    }

    private List<QualityIssueVO> loadIssues(QualityRule rule) {
        Supplier<List<QualityIssueVO>> supplier = registry.get(rule);
        if (supplier == null) {
            throw new BusinessException("规则 " + rule.getCode() + " 没有对应的取数实现");
        }
        List<QualityIssueVO> rows = supplier.get();
        if (rows == null) {
            return List.of();
        }
        if (rows.size() > MAX_ROWS_PER_RULE) {
            log.warn("数据质量规则 {} 命中 {} 条，超过单规则上限 {}，已截断明细（总览数字仍为全量）",
                    rule.getCode(), rows.size(), MAX_ROWS_PER_RULE);
            return rows.subList(0, MAX_ROWS_PER_RULE);
        }
        return rows;
    }

    /**
     * 把规则元信息与分母补进明细行，让每一条问题都能独立读懂
     */
    private void decorate(QualityIssueVO v, QualityRule rule, long checked) {
        v.setRuleCode(rule.getCode());
        v.setRuleName(rule.getName());
        v.setDimension(rule.getDimension().name());
        v.setDimensionText(rule.getDimension().getText());
        v.setSeverity(rule.getSeverity().getLevel());
        v.setSeverityText(rule.getSeverity().getText());
        v.setSuggestion(rule.getSuggestion());
        v.setCheckedTotal(checked);
        if (!TextUtil.hasText(v.getTableName())) {
            v.setTableName(rule.getTableName());
        }
    }

    private boolean matchKeyword(QualityIssueVO v, String keyword) {
        if (!TextUtil.hasText(keyword)) {
            return true;
        }
        String k = keyword.trim();
        return contains(v.getPatientNo(), k) || contains(v.getPatientName(), k)
                || contains(v.getRecordNo(), k) || contains(v.getDetail(), k);
    }

    private List<QualityRuleVO> ruleVOs(Map<String, QualityRuleTotalVO> totals) {
        return Arrays.stream(QualityRule.values())
                .map(r -> toRuleVO(r, totals.get(r.getCode())))
                .collect(Collectors.toList());
    }

    private QualityRuleVO toRuleVO(QualityRule r, QualityRuleTotalVO t) {
        long checked = t == null || t.getCheckedTotal() == null ? 0 : t.getCheckedTotal();
        long hit = t == null || t.getHitTotal() == null ? 0 : t.getHitTotal();
        QualityRuleVO vo = new QualityRuleVO();
        vo.setRuleCode(r.getCode());
        vo.setRuleName(r.getName());
        vo.setDimension(r.getDimension().name());
        vo.setDimensionText(r.getDimension().getText());
        vo.setSeverity(r.getSeverity().getLevel());
        vo.setSeverityText(r.getSeverity().getText());
        vo.setTableName(r.getTableName());
        vo.setCheckedDesc(r.getCheckedDesc());
        vo.setSuggestion(r.getSuggestion());
        vo.setBasis(r.getBasis());
        vo.setCheckedTotal(checked);
        vo.setIssueCount(hit);
        vo.setEmpty(checked == 0);
        vo.setHasIssue(hit > 0);
        vo.setPassRate(rate(checked, hit));
        return vo;
    }

    private long checkedOf(Map<String, QualityRuleTotalVO> totals, String code) {
        QualityRuleTotalVO t = totals.get(code);
        return t == null || t.getCheckedTotal() == null ? 0 : t.getCheckedTotal();
    }

    /**
     * 合规率 %：分母为 0 时返回 null（不是 0，也不是 100 —— 那是"没查"，不是"干净"）
     */
    private BigDecimal rate(Long checked, Long issues) {
        if (checked == null || checked <= 0) {
            return null;
        }
        long bad = issues == null ? 0 : issues;
        return BigDecimal.valueOf(Math.max(0, checked - bad))
                .multiply(HUNDRED)
                .divide(BigDecimal.valueOf(checked), 1, RoundingMode.HALF_UP);
    }

    private long sum(List<QualityRuleVO> rules, java.util.function.Function<QualityRuleVO, Long> getter) {
        return rules.stream().map(getter).filter(java.util.Objects::nonNull).mapToLong(Long::longValue).sum();
    }
}

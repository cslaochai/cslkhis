package com.mis.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mis.entity.JinbSettlement;
import com.mis.entity.MssAuditRule;
import com.mis.entity.MssBill;
import com.mis.entity.MssCatalog;
import com.mis.entity.MssFeeItem;
import com.mis.mapper.JinbSettlementMapper;
import com.mis.mapper.MssBillMapper;
import com.mis.mapper.MssCatalogMapper;
import com.mis.mapper.MssFeeItemMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 基金监管规则引擎：按启用规则扫描经办结算单 / 机构报盘数据，产出疑点候选。
 * <p>扫描结果只计算不落库（幂等判重由 SuspicionServiceImpl 负责）。
 * 规则参数从 mss_audit_rule.params（JSON）读取，缺省用各规则内置默认值。
 */
@Component
@RequiredArgsConstructor
public class SuperviseRuleEngine {

    private final MssBillMapper billMapper;
    private final MssFeeItemMapper feeItemMapper;
    private final MssCatalogMapper catalogMapper;
    private final JinbSettlementMapper settlementMapper;
    private final ObjectMapper objectMapper;

    /** 疑点候选（对应 mss_suspicion 各列） */
    @Data
    public static class Draft {
        private Long ruleId;
        private String ruleName;
        private String ruleType;
        private Integer bizSource;
        private Long bizId;
        private String bizNo;
        private String patientName;
        private String idCardMask;
        private String insuranceType;
        private String encounterType;
        private String dept;
        private String doctor;
        private LocalDate bizDate;
        private BigDecimal involvedAmount;
        private String reason;
    }

    public List<Draft> scan(List<MssAuditRule> rules) {
        List<Draft> drafts = new ArrayList<>();
        for (MssAuditRule r : rules) {
            if (r.getStatus() == null || r.getStatus() != 1) {
                continue;
            }
            String type = r.getRuleType();
            switch (type == null ? "" : type) {
                case "duplicate_charge" -> drafts.addAll(duplicateCharge(r));
                case "over_limit_price" -> drafts.addAll(overLimitPrice(r));
                case "split_hospitalization" -> drafts.addAll(splitHospitalization(r));
                case "bed_hanging" -> drafts.addAll(bedHanging(r));
                case "abnormal_cost" -> drafts.addAll(abnormalCost(r));
                case "payclass_c" -> drafts.addAll(payclassC(r));
                default -> {
                    // 未知规则类型跳过
                }
            }
        }
        return drafts;
    }

    // ------------------------------------------------------------ 规则实现

    /** 重复收费：同一结算单同项目多次计费且合计达阈值 */
    private List<Draft> duplicateCharge(MssAuditRule r) {
        BigDecimal minAmount = param(r, "minAmount", new BigDecimal("200"));
        List<Draft> out = new ArrayList<>();
        List<MssBill> bills = billMapper.selectList(null);
        Map<Long, List<MssFeeItem>> byBill = groupFeeItemsByBill();
        for (MssBill b : bills) {
            List<MssFeeItem> items = byBill.getOrDefault(b.getId(), List.of());
            Map<String, List<MssFeeItem>> byName = new LinkedHashMap<>();
            for (MssFeeItem it : items) {
                String key = nz(it.getName()) + "|" + nz(it.getCategory());
                byName.computeIfAbsent(key, k -> new ArrayList<>()).add(it);
            }
            for (Map.Entry<String, List<MssFeeItem>> e : byName.entrySet()) {
                List<MssFeeItem> same = e.getValue();
                BigDecimal sum = sumAmount(same);
                if (same.size() > 1 && sum.compareTo(minAmount) >= 0) {
                    Draft d = baseDraft(r, b);
                    d.setInvolvedAmount(sum);
                    d.setReason(String.format("同一结算单存在重复收费项目「%s」%d 次，合计 %s 元，涉嫌重复收费",
                            nz(same.get(0).getName()), same.size(), plain(sum)));
                    out.add(d);
                }
            }
        }
        return out;
    }

    /** 超限价收费：项目单价高于医保限价（超限价比例阈值） */
    private List<Draft> overLimitPrice(MssAuditRule r) {
        BigDecimal overRatio = param(r, "overRatio", new BigDecimal("0.1"));
        List<Draft> out = new ArrayList<>();
        Map<String, MssCatalog> catalogByCode = new HashMap<>();
        for (MssCatalog c : catalogMapper.selectList(null)) {
            if (c.getLimitPrice() != null && c.getLimitPrice().compareTo(BigDecimal.ZERO) > 0) {
                catalogByCode.put(nz(c.getYbCode()), c);
            }
        }
        List<MssBill> bills = billMapper.selectList(null);
        Map<Long, MssBill> billById = new HashMap<>();
        for (MssBill b : bills) {
            billById.put(b.getId(), b);
        }
        List<MssFeeItem> items = feeItemMapper.selectList(null);
        for (MssFeeItem it : items) {
            if (it.getOwnerType() == null || it.getOwnerType() != 2) {
                continue;
            }
            MssCatalog c = catalogByCode.get(nz(it.getYbCode()));
            if (c == null || it.getPrice() == null || it.getQty() == null) {
                continue;
            }
            BigDecimal over = c.getLimitPrice().multiply(BigDecimal.ONE.add(overRatio));
            if (it.getPrice().compareTo(over) > 0) {
                MssBill b = billById.get(it.getOwnerId());
                if (b == null) {
                    continue;
                }
                BigDecimal involved = it.getPrice().subtract(c.getLimitPrice()).multiply(BigDecimal.valueOf(it.getQty()));
                Draft d = baseDraft(r, b);
                d.setInvolvedAmount(involved);
                d.setReason(String.format("项目「%s」单价 %s 元，高于医保限价 %s 元（超限价比例 %s%%），涉及 %s 元",
                        nz(it.getName()), plain(it.getPrice()), plain(c.getLimitPrice()),
                        overRatio.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString(), plain(involved)));
                out.add(d);
            }
        }
        return out;
    }

    /** 分解住院：同一参保人窗口期内多次住院结算 */
    private List<Draft> splitHospitalization(MssAuditRule r) {
        int windowDays = paramInt(r, "windowDays", 15);
        int minCount = paramInt(r, "minCount", 2);
        List<Draft> out = new ArrayList<>();
        List<JinbSettlement> all = settlementMapper.selectList(null);
        Map<String, List<JinbSettlement>> byCard = new HashMap<>();
        for (JinbSettlement s : all) {
            if (s.getStatus() != null && s.getStatus() == 2) {
                continue; // 已撤销冲正不参与
            }
            if (!"住院".equals(s.getEncounterType())) {
                continue;
            }
            if (s.getIdCard() == null || s.getIdCard().isBlank()) {
                continue;
            }
            byCard.computeIfAbsent(s.getIdCard(), k -> new ArrayList<>()).add(s);
        }
        for (Map.Entry<String, List<JinbSettlement>> e : byCard.entrySet()) {
            List<JinbSettlement> list = e.getValue();
            list.sort((a, b) -> nz(a.getBillDate()).compareTo(nz(b.getBillDate())));
            int runStart = 0;
            int runCount = 1;
            for (int i = 1; i < list.size(); i++) {
                LocalDate prev = nz(list.get(i - 1).getBillDate());
                LocalDate cur = nz(list.get(i).getBillDate());
                boolean within = prev != null && cur != null && ChronoUnit.DAYS.between(prev, cur) <= windowDays;
                if (within) {
                    runCount++;
                } else {
                    if (runCount >= minCount) {
                        out.add(splitDraft(r, list, runStart, runCount, windowDays));
                    }
                    runStart = i;
                    runCount = 1;
                }
            }
            if (runCount >= minCount) {
                out.add(splitDraft(r, list, runStart, runCount, windowDays));
            }
        }
        return out;
    }

    private Draft splitDraft(MssAuditRule r, List<JinbSettlement> list, int start, int count, int windowDays) {
        JinbSettlement first = list.get(start);
        BigDecimal involved = BigDecimal.ZERO;
        StringBuilder dates = new StringBuilder();
        for (int i = start; i < start + count; i++) {
            involved = involved.add(nz(firstPay(list.get(i))));
            if (dates.length() > 0) {
                dates.append("、");
            }
            dates.append(nz(list.get(i).getBillDate()));
        }
        Draft d = new Draft();
        d.setRuleId(r.getId());
        d.setRuleName(r.getRuleName());
        d.setRuleType(r.getRuleType());
        d.setBizSource(2);
        d.setBizId(first.getId());
        d.setBizNo(first.getTradeNo());
        d.setPatientName(first.getPatientName());
        d.setIdCardMask(mask(first.getIdCard()));
        d.setInsuranceType(first.getInsuranceType());
        d.setEncounterType("住院");
        d.setBizDate(nz(first.getBillDate()));
        d.setInvolvedAmount(involved);
        d.setReason(String.format("同一参保人 %d 天内住院结算 %d 次（%s），涉嫌分解住院，涉及统筹 %s 元",
                windowDays, count, dates, plain(involved)));
        return d;
    }

    /** 挂床住院：住院天数长而费用极低 */
    private List<Draft> bedHanging(MssAuditRule r) {
        int minDays = paramInt(r, "minDays", 10);
        BigDecimal maxAmount = param(r, "maxAmount", new BigDecimal("500"));
        List<Draft> out = new ArrayList<>();
        for (MssBill b : billMapper.selectList(null)) {
            if (!"住院".equals(b.getSettleType()) || b.getAdmitDays() == null) {
                continue;
            }
            BigDecimal total = nz(b.getTotalAmount());
            if (b.getAdmitDays() >= minDays && total.compareTo(maxAmount) <= 0) {
                Draft d = baseDraft(r, b);
                d.setInvolvedAmount(total);
                d.setReason(String.format("住院 %d 天费用仅 %s 元，涉嫌挂床住院", b.getAdmitDays(), plain(total)));
                out.add(d);
            }
        }
        return out;
    }

    /** 高额住院费用：住院费用显著高于同险种均值 */
    private List<Draft> abnormalCost(MssAuditRule r) {
        BigDecimal avgTimes = param(r, "avgTimes", new BigDecimal("3"));
        List<Draft> out = new ArrayList<>();
        List<JinbSettlement> all = settlementMapper.selectList(null);
        Map<String, BigDecimal> sumByIns = new HashMap<>();
        Map<String, Integer> cntByIns = new HashMap<>();
        for (JinbSettlement s : all) {
            if (s.getStatus() != null && s.getStatus() == 2) {
                continue;
            }
            if (!"住院".equals(s.getEncounterType())) {
                continue;
            }
            String ins = nz(s.getInsuranceType());
            sumByIns.merge(ins, nz(s.getTotalAmount()), BigDecimal::add);
            cntByIns.merge(ins, 1, Integer::sum);
        }
        Map<String, BigDecimal> avgByIns = new HashMap<>();
        for (Map.Entry<String, Integer> e : cntByIns.entrySet()) {
            BigDecimal avg = sumByIns.get(e.getKey()).divide(BigDecimal.valueOf(e.getValue()), 2, RoundingMode.HALF_UP);
            avgByIns.put(e.getKey(), avg);
        }
        for (JinbSettlement s : all) {
            if (s.getStatus() != null && s.getStatus() == 2 || !"住院".equals(s.getEncounterType())) {
                continue;
            }
            BigDecimal avg = avgByIns.get(nz(s.getInsuranceType()));
            if (avg == null || avg.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal threshold = avg.multiply(avgTimes);
            BigDecimal total = nz(s.getTotalAmount());
            if (total.compareTo(threshold) > 0) {
                Draft d = new Draft();
                d.setRuleId(r.getId());
                d.setRuleName(r.getRuleName());
                d.setRuleType(r.getRuleType());
                d.setBizSource(2);
                d.setBizId(s.getId());
                d.setBizNo(s.getTradeNo());
                d.setPatientName(s.getPatientName());
                d.setIdCardMask(mask(s.getIdCard()));
                d.setInsuranceType(s.getInsuranceType());
                d.setEncounterType("住院");
                d.setBizDate(nz(s.getBillDate()));
                d.setInvolvedAmount(total);
                d.setReason(String.format("住院费用 %s 元，高于同险种均值 %s 元的 %s 倍，涉嫌高额虚记",
                        plain(total), plain(avg), avgTimes.stripTrailingZeros().toPlainString()));
                out.add(d);
            }
        }
        return out;
    }

    /** 丙类项目入统筹：结算单含丙类项目金额并发生统筹支付 */
    private List<Draft> payclassC(MssAuditRule r) {
        BigDecimal minAmount = param(r, "minAmount", new BigDecimal("200"));
        List<Draft> out = new ArrayList<>();
        List<MssBill> bills = billMapper.selectList(null);
        Map<Long, List<MssFeeItem>> byBill = groupFeeItemsByBill();
        for (MssBill b : bills) {
            if (nz(b.getInsurancePay()).compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal cSum = BigDecimal.ZERO;
            for (MssFeeItem it : byBill.getOrDefault(b.getId(), List.of())) {
                if ("丙".equals(it.getPayClass())) {
                    cSum = cSum.add(nz(it.getAmount()));
                }
            }
            if (cSum.compareTo(minAmount) >= 0) {
                Draft d = baseDraft(r, b);
                d.setInvolvedAmount(cSum);
                d.setReason(String.format("结算单含丙类项目 %s 元且发生统筹支付 %s 元，涉嫌串换项目/虚记费用",
                        plain(cSum), plain(b.getInsurancePay())));
                out.add(d);
            }
        }
        return out;
    }

    // ------------------------------------------------------------ 工具

    private Map<Long, List<MssFeeItem>> groupFeeItemsByBill() {
        Map<Long, List<MssFeeItem>> map = new HashMap<>();
        for (MssFeeItem it : feeItemMapper.selectList(null)) {
            if (it.getOwnerType() != null && it.getOwnerType() == 2) {
                map.computeIfAbsent(it.getOwnerId(), k -> new ArrayList<>()).add(it);
            }
        }
        return map;
    }

    private Draft baseDraft(MssAuditRule r, MssBill b) {
        Draft d = new Draft();
        d.setRuleId(r.getId());
        d.setRuleName(r.getRuleName());
        d.setRuleType(r.getRuleType());
        d.setBizSource(1);
        d.setBizId(b.getId());
        d.setBizNo(b.getBillNo());
        d.setPatientName(b.getPatientName());
        d.setIdCardMask(b.getIdCardMask() != null ? b.getIdCardMask() : mask(b.getIdCard()));
        d.setInsuranceType(b.getInsuranceType());
        d.setEncounterType(b.getSettleType());
        d.setDept(b.getDept());
        d.setDoctor(b.getDoctor());
        d.setBizDate(b.getSettleTime() == null ? null : b.getSettleTime().toLocalDate());
        d.setInvolvedAmount(nz(b.getInsurancePay()));
        return d;
    }

    private BigDecimal param(MssAuditRule r, String key, BigDecimal def) {
        try {
            Map<?, ?> map = objectMapper.readValue(r.getParams(), Map.class);
            Object v = map.get(key);
            if (v != null) {
                return new BigDecimal(v.toString());
            }
        } catch (Exception ignored) {
            // 参数缺失/格式错用默认值
        }
        return def;
    }

    private int paramInt(MssAuditRule r, String key, int def) {
        try {
            Map<?, ?> map = objectMapper.readValue(r.getParams(), Map.class);
            Object v = map.get(key);
            if (v != null) {
                return (int) Double.parseDouble(v.toString());
            }
        } catch (Exception ignored) {
            // 参数缺失/格式错用默认值
        }
        return def;
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO.setScale(2) : v;
    }

    private LocalDate nz(LocalDate d) {
        return d == null ? LocalDate.now() : d;
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }

    private BigDecimal sumAmount(List<MssFeeItem> items) {
        BigDecimal sum = BigDecimal.ZERO;
        for (MssFeeItem it : items) {
            sum = sum.add(nz(it.getAmount()));
        }
        return sum;
    }

    private BigDecimal firstPay(JinbSettlement s) {
        return nz(s.getInsurancePay());
    }

    private String mask(String idCard) {
        if (idCard == null || idCard.length() <= 8) {
            return idCard;
        }
        int stars = idCard.length() - 8;
        return idCard.substring(0, 4) + "*".repeat(stars) + idCard.substring(idCard.length() - 4);
    }

    private String plain(BigDecimal v) {
        return v == null ? "0.00" : v.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}

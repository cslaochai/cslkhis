package com.his.charge.support;

import com.his.charge.entity.BizInsuranceSettlement;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * C 组：住院指征（低编入组 / 低标入院 / 分解住院）。
 *
 * <p><b>必须说清楚的边界</b>：本系统<b>没有住院管理模块</b>（`ward` 只是病区字典，
 * 挂号表里没有入院/出院时间），所以「住院天数」在数据上根本不存在。
 * 缺数据时 C01 一律返回「不适用」并写明原因，绝不按 0 天默认成「不达标」或默认成「达标」——
 * 前者会满屏误报，后者会把真正的低标入院放过去。</p>
 */
@Component
public class AdmissionIndicationRule implements ComplianceRule {

    private static String dateOf(BizInsuranceSettlement s) {
        return s.getCreateTime() == null ? "日期不详" : s.getCreateTime().toLocalDate().toString();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    @Override
    public String group() {
        return "C";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext ctx) {
        List<RuleFinding> findings = new ArrayList<>();
        evaluateC01(ctx, findings);
        evaluateC02(ctx, findings);
        evaluateC03(ctx, findings);
        return findings;
    }

    /**
     * C01 住院天数不足且无手术操作
     */
    private void evaluateC01(RuleContext ctx, List<RuleFinding> findings) {
        // 本系统未记录入出院时间，无法计算住院天数 —— 这是数据缺口，不是「通过」
        boolean hasOperation = !ctx.getOperations().isEmpty();
        if (hasOperation) {
            findings.add(RuleFinding.na(RuleCatalog.C01,
                    "清单含手术操作，不属低标入院可疑范围（且本系统无住院管理模块，无法计算住院天数）"));
            return;
        }
        findings.add(RuleFinding.na(RuleCatalog.C01,
                        "本系统未记录入院/出院时间（无住院管理模块），无法计算住院天数，"
                                + "该规则暂不可评估；需接入住院管理系统后方可启用")
                .withSuggestion("接入住院管理（入院/出院时间、床日）后，本规则可自动评估低标入院与低编入组"));
    }

    /**
     * C02 无住院级别诊疗行为
     */
    private void evaluateC02(RuleContext ctx, List<RuleFinding> findings) {
        SettlementEvidence ev = ctx.getEvidence();
        boolean noData = ev.getBillItems().isEmpty()
                && ev.getPrescriptions().isEmpty()
                && !ev.hasAnyLabOrInspection();
        if (noData) {
            findings.add(RuleFinding.na(RuleCatalog.C02,
                    "无收费、处方、检验检查数据，无法评估诊疗行为强度"));
            return;
        }
        boolean hasTreatment = ev.hasTreatmentCharge();
        boolean hasLabOrInspection = ev.hasAnyLabOrInspection();
        if (!hasTreatment && !hasLabOrInspection) {
            List<String> names = ev.getBillItems().stream()
                    .map(d -> d.getItemName())
                    .filter(StringUtils::hasText)
                    .limit(5)
                    .collect(Collectors.toList());
            findings.add(RuleFinding.hit(RuleCatalog.C02,
                    "未见手术、检验、检查及治疗性收费，仅有药品/挂号费类项目："
                            + (names.isEmpty() ? "（无项目名）" : String.join("、", names))));
            return;
        }
        findings.add(RuleFinding.pass(RuleCatalog.C02,
                "存在住院级别诊疗行为（治疗性收费=" + hasTreatment + "，检验检查=" + hasLabOrInspection + "）"));
    }

    /**
     * C03 疑似分解住院
     */
    private void evaluateC03(RuleContext ctx, List<RuleFinding> findings) {
        BizInsuranceSettlement settlement = ctx.getSettlement();
        Long patientId = settlement == null ? null : settlement.getPatientId();
        if (patientId == null) {
            findings.add(RuleFinding.na(RuleCatalog.C03, "清单未填患者ID，无法比对历史住院"));
            return;
        }
        String diagCode = currentMainDiagCode(ctx);
        if (!StringUtils.hasText(diagCode)) {
            findings.add(RuleFinding.na(RuleCatalog.C03, "主诊断编码为空，无法比对是否分解住院"));
            return;
        }
        if (CollectionUtils.isEmpty(ctx.getRecentSettlements())) {
            findings.add(RuleFinding.pass(RuleCatalog.C03,
                    "窗口期内无同患者其他结算清单（窗口 " + windowDays(ctx) + " 天）"));
            return;
        }
        List<String> dups = new ArrayList<>();
        for (BizInsuranceSettlement other : ctx.getRecentSettlements()) {
            String otherCode = StringUtils.hasText(other.getDiagnosisCode())
                    ? other.getDiagnosisCode()
                    : other.getDiagnosis();
            if (StringUtils.hasText(otherCode) && otherCode.equalsIgnoreCase(diagCode)) {
                dups.add(safe(other.getSettlementNo()) + "(" + dateOf(other) + ")");
            }
        }
        if (dups.isEmpty()) {
            findings.add(RuleFinding.pass(RuleCatalog.C03,
                    "窗口期内 " + ctx.getRecentSettlements().size() + " 条同患者结算清单，主诊断均不同"));
            return;
        }
        findings.add(RuleFinding.hit(RuleCatalog.C03,
                "窗口 " + windowDays(ctx) + " 天内该患者以相同主诊断 " + diagCode + " 再次结算："
                        + String.join("、", dups)));
    }

    private String currentMainDiagCode(RuleContext ctx) {
        if (ctx.mainDiagnosis() != null && StringUtils.hasText(ctx.mainDiagnosis().getIcdCode())) {
            return ctx.mainDiagnosis().getIcdCode();
        }
        BizInsuranceSettlement s = ctx.getSettlement();
        if (s == null) {
            return null;
        }
        return StringUtils.hasText(s.getDiagnosisCode()) ? s.getDiagnosisCode() : null;
    }

    private int windowDays(RuleContext ctx) {
        return ctx.getProperties() == null ? 15 : ctx.getProperties().getReadmitWindowDays();
    }
}

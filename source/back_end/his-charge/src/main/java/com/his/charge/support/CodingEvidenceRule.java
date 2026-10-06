package com.his.charge.support;


import com.his.charge.entity.BizSettlementBillItem;
import com.his.charge.entity.BizSettlementDiagnosis;
import com.his.charge.entity.BizSettlementOperation;
import com.his.charge.enums.RuleCatalogEnum;
import com.his.charge.vo.InspectionRecordBriefVO;
import com.his.charge.vo.LabResultBriefVO;
import com.his.charge.vo.LaboratoryRecordBriefVO;
import com.his.charge.vo.MedicalRecordBriefVO;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A 组：编码依据一致性。
 *
 * <p>高编高套的本质是「编了没有依据的编码」，所以这一组是主力：
 * A01~A05 查编码本身是否齐备、是否在目录内、是否与病历一致；
 * A06/A07 查手术编码有没有依据、有手术费有没有编码；
 * A08 反过来查「有依据却没编码」——这就是低编入组。</p>
 */
@Component
public class CodingEvidenceRule implements ComplianceRule {

    /**
     * 手术行为关键词（治疗类收费项名里出现即视为手术性收费）
     */
    private static final String[] SURGERY_ACTIONS = {
            "手术", "切除", "吻合", "置换", "置入", "植入", "镜", "造影", "引流",
            "缝合", "固定", "修补", "成形", "结扎", "穿刺", "造口", "清创", "复位"
    };

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    @Override
    public String group() {
        return "A";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext ctx) {
        List<RuleFinding> findings = new ArrayList<>();
        BizSettlementDiagnosis main = ctx.mainDiagnosis();

        evaluateA01(ctx, main, findings);
        evaluateA02(ctx, main, findings);
        evaluateA03(ctx, main, findings);
        evaluateA04(ctx, findings);
        evaluateA05(ctx, findings);
        evaluateA06(ctx, findings);
        evaluateA07(ctx, findings);
        evaluateA08(ctx, findings);

        return findings;
    }

    /**
     * A01 主要诊断未编码
     */
    private void evaluateA01(RuleContext ctx, BizSettlementDiagnosis main, List<RuleFinding> findings) {
        if (main == null) {
            findings.add(RuleFinding.na(RuleCatalogEnum.A01, "清单无诊断明细，无法评估主诊断编码"));
            return;
        }
        if (!StringUtils.hasText(main.getIcdCode())) {
            findings.add(RuleFinding.hit(RuleCatalogEnum.A01,
                            "主要诊断「" + safe(main.getIcdName()) + "」未填 ICD-10 编码")
                    .on(1, main.getId(), main.getIcdCode(), main.getIcdName()));
            return;
        }
        findings.add(RuleFinding.pass(RuleCatalogEnum.A01,
                        "主要诊断已编码：" + main.getIcdCode() + " " + safe(main.getIcdName()))
                .on(1, main.getId(), main.getIcdCode(), main.getIcdName()));
    }

    /**
     * A02 主要诊断编码不在医保目录内
     */
    private void evaluateA02(RuleContext ctx, BizSettlementDiagnosis main, List<RuleFinding> findings) {
        if (main == null || !StringUtils.hasText(main.getIcdCode())) {
            findings.add(RuleFinding.na(RuleCatalogEnum.A02, "主诊断未编码，无从校验目录归属"));
            return;
        }
        if (CollectionUtils.isEmpty(ctx.getEnabledIcdCodes())) {
            findings.add(RuleFinding.na(RuleCatalogEnum.A02,
                            "本院启用的医保 ICD-10 目录为空，无法校验编码合法性（目录未启用时不得默认判过）")
                    .on(1, main.getId(), main.getIcdCode(), main.getIcdName()));
            return;
        }
        if (!ctx.getEnabledIcdCodes().contains(main.getIcdCode())) {
            findings.add(RuleFinding.hit(RuleCatalogEnum.A02,
                            "主诊断编码 " + main.getIcdCode() + " 不在本院启用的医保目录（共 "
                                    + ctx.getEnabledIcdCodes().size() + " 条）中")
                    .on(1, main.getId(), main.getIcdCode(), main.getIcdName()));
            return;
        }
        findings.add(RuleFinding.pass(RuleCatalogEnum.A02, "主诊断编码在医保目录内")
                .on(1, main.getId(), main.getIcdCode(), main.getIcdName()));
    }

    /**
     * A03 主要诊断与病历诊断不一致
     */
    private void evaluateA03(RuleContext ctx, BizSettlementDiagnosis main, List<RuleFinding> findings) {
        MedicalRecordBriefVO record = ctx.getEvidence().getMedicalRecord();
        if (record == null) {
            findings.add(RuleFinding.na(RuleCatalogEnum.A03, "查无病历记录，无法比对清单与病历诊断"));
            return;
        }
        if (main == null || !StringUtils.hasText(main.getIcdCode())) {
            findings.add(RuleFinding.na(RuleCatalogEnum.A03, "清单主诊断未编码，无从比对"));
            return;
        }
        if (!StringUtils.hasText(record.getDiagnosisCode())) {
            findings.add(RuleFinding.na(RuleCatalogEnum.A03,
                            "病历（" + safe(record.getRecordNo()) + "）未填诊断编码，无法比对")
                    .on(1, main.getId(), main.getIcdCode(), main.getIcdName()));
            return;
        }
        if (!main.getIcdCode().equalsIgnoreCase(record.getDiagnosisCode())) {
            findings.add(RuleFinding.hit(RuleCatalogEnum.A03,
                            "清单主诊断为 " + main.getIcdCode() + "，病历诊断为 " + record.getDiagnosisCode())
                    .on(1, main.getId(), main.getIcdCode(), main.getIcdName()));
            return;
        }
        findings.add(RuleFinding.pass(RuleCatalogEnum.A03, "清单主诊断与病历诊断一致（" + record.getDiagnosisCode() + "）")
                .on(1, main.getId(), main.getIcdCode(), main.getIcdName()));
    }

    /**
     * A04 其他诊断缺编码
     */
    private void evaluateA04(RuleContext ctx, List<RuleFinding> findings) {
        List<BizSettlementDiagnosis> others = ctx.getDiagnoses().stream()
                .filter(d -> d.getDiagType() != null && d.getDiagType() == 2)
                .collect(Collectors.toList());
        if (others.isEmpty()) {
            findings.add(RuleFinding.na(RuleCatalogEnum.A04, "清单无其他诊断，无需评估"));
            return;
        }
        for (BizSettlementDiagnosis d : others) {
            if (!StringUtils.hasText(d.getIcdCode())) {
                findings.add(RuleFinding.hit(RuleCatalogEnum.A04,
                                "其他诊断「" + safe(d.getIcdName()) + "」未填 ICD-10 编码")
                        .on(1, d.getId(), d.getIcdCode(), d.getIcdName()));
            } else {
                findings.add(RuleFinding.pass(RuleCatalogEnum.A04, "其他诊断已编码：" + d.getIcdCode())
                        .on(1, d.getId(), d.getIcdCode(), d.getIcdName()));
            }
        }
    }

    /**
     * A05 手术操作未编码
     */
    private void evaluateA05(RuleContext ctx, List<RuleFinding> findings) {
        if (ctx.getOperations().isEmpty()) {
            findings.add(RuleFinding.na(RuleCatalogEnum.A05, "清单无手术操作明细，无需评估"));
            return;
        }
        for (BizSettlementOperation o : ctx.getOperations()) {
            if (!StringUtils.hasText(o.getOperCode())) {
                findings.add(RuleFinding.hit(RuleCatalogEnum.A05,
                                "手术操作「" + safe(o.getOperName()) + "」未填 ICD-9-CM-3 编码")
                        .on(2, o.getId(), o.getOperCode(), o.getOperName()));
            } else {
                findings.add(RuleFinding.pass(RuleCatalogEnum.A05, "手术操作已编码：" + o.getOperCode())
                        .on(2, o.getId(), o.getOperCode(), o.getOperName()));
            }
        }
    }

    /**
     * A06 手术操作无病历与收费依据
     */
    private void evaluateA06(RuleContext ctx, List<RuleFinding> findings) {
        if (ctx.getOperations().isEmpty()) {
            findings.add(RuleFinding.na(RuleCatalogEnum.A06, "清单无手术操作明细，无需评估"));
            return;
        }
        SettlementEvidence ev = ctx.getEvidence();
        String text = ev.surgicalEvidenceText();
        if (!StringUtils.hasText(text)) {
            findings.add(RuleFinding.na(RuleCatalogEnum.A06,
                    "本次就诊无病历与收费/诊疗项目数据，无法核对手术依据"));
            return;
        }

        for (BizSettlementOperation o : ctx.getOperations()) {
            String name = o.getOperName();
            if (!StringUtils.hasText(name)) {
                if (!StringUtils.hasText(o.getOperCode())) {
                    findings.add(RuleFinding.na(RuleCatalogEnum.A06, "手术操作无名称无编码，无法核对依据")
                            .on(2, o.getId(), o.getOperCode(), o.getOperName()));
                    continue;
                }
                name = o.getOperCode();
            }
            // 退化「术」「手术」等泛词后再匹配，避免靠一个「术」字就判过
            String core = name.replaceAll("(手术|术式|术|治疗)$", "");
            boolean exact = EvidenceKeywordMatcher.hits(text, name)
                    || (StringUtils.hasText(core) && !core.equals(name) && EvidenceKeywordMatcher.hits(text, core));
            boolean action = !exact && EvidenceKeywordMatcher.hits(text, SURGERY_ACTIONS);

            if (exact) {
                findings.add(RuleFinding.pass(RuleCatalogEnum.A06,
                                "在病历或诊疗项目中匹配到「" + name + "」")
                        .on(2, o.getId(), o.getOperCode(), o.getOperName()));
            } else if (action) {
                findings.add(RuleFinding.pass(RuleCatalogEnum.A06,
                                "未精确匹配到「" + name + "」，但存在手术行为描述，建议人工复核")
                        .on(2, o.getId(), o.getOperCode(), o.getOperName()));
            } else {
                findings.add(RuleFinding.hit(RuleCatalogEnum.A06,
                                "病历、处方、检查检验及收费明细中均未找到「" + name + "」的任何记载")
                        .on(2, o.getId(), o.getOperCode(), o.getOperName()));
            }
        }
    }

    /**
     * A07 有手术性治疗费用但无手术编码（漏编）
     */
    private void evaluateA07(RuleContext ctx, List<RuleFinding> findings) {
        SettlementEvidence ev = ctx.getEvidence();
        boolean anyCoded = ctx.getOperations().stream()
                .anyMatch(o -> StringUtils.hasText(o.getOperCode()));
        if (anyCoded) {
            findings.add(RuleFinding.pass(RuleCatalogEnum.A07,
                    "已编 " + ctx.getOperations().size() + " 条手术操作，费用与编码齐备"));
            return;
        }
        if (ev.getBillItems().isEmpty()) {
            findings.add(RuleFinding.na(RuleCatalogEnum.A07, "无账单行，无法核对手术性费用"));
            return;
        }
        List<BizSettlementBillItem> surgical = ev.surgicalTreatmentCharges(SURGERY_ACTIONS);
        if (surgical.isEmpty()) {
            findings.add(RuleFinding.pass(RuleCatalogEnum.A07, "账单行中未发现手术性治疗项目"));
            return;
        }
        String names = surgical.stream()
                .map(d -> safe(d.getItemName()) + "(" + d.getAmount() + "元)")
                .limit(5)
                .collect(Collectors.joining("、"));
        findings.add(RuleFinding.hit(RuleCatalogEnum.A07,
                "存在 " + surgical.size() + " 项手术性治疗收费但无任何手术操作编码：" + names));
    }

    /**
     * A08 存在漏编诊断（低编入组）
     */
    private void evaluateA08(RuleContext ctx, List<RuleFinding> findings) {
        SettlementEvidence ev = ctx.getEvidence();
        String evidenceText = labInspectionText(ev);
        if (!StringUtils.hasText(evidenceText)) {
            findings.add(RuleFinding.na(RuleCatalogEnum.A08,
                    "本次就诊无检验检查数据，无法评估是否存在漏编诊断"));
            return;
        }

        List<RuleFinding> hits = new ArrayList<>();
        List<String> hitDetails = new ArrayList<>();
        for (LowCodingEvidenceDict.Entry entry : LowCodingEvidenceDict.entries()) {
            if (!EvidenceKeywordMatcher.hits(evidenceText, entry.getEvidenceKeyword())) {
                continue;
            }
            boolean coded = ctx.getDiagnoses().stream().anyMatch(d ->
                    StringUtils.hasText(d.getIcdCode())
                            && entry.getIcdPrefix() != null
                            && d.getIcdCode().toUpperCase().startsWith(entry.getIcdPrefix().toUpperCase()));
            if (!coded) {
                hitDetails.add(entry.getEvidenceKeyword() + "→" + entry.getHintDiagnosis()
                        + "(" + entry.getIcdPrefix() + "*)");
                RuleFinding f = RuleFinding.hit(RuleCatalogEnum.A08,
                                "检验/检查证据提示 " + entry.getHintDiagnosis() + "，但清单未编该诊断"
                                        + "（命中依据：" + entry.getEvidenceKeyword() + "，建议编码前缀 "
                                        + entry.getIcdPrefix() + "）")
                        .withSuggestion("核实是否漏编「" + entry.getHintDiagnosis()
                                + "」；若成立请补编，否则请在病历中说明该检查的临床意义");
                hits.add(f);
            }
        }

        if (hits.isEmpty()) {
            findings.add(RuleFinding.pass(RuleCatalogEnum.A08,
                    "检验/检查证据中未发现「有依据但未编码」的诊断"));
        } else {
            findings.addAll(hits);
        }
    }

    /**
     * 检验检查证据文本：项目名 + 异常描述 + 结论 + 建议
     */
    private String labInspectionText(SettlementEvidence ev) {
        StringBuilder sb = new StringBuilder();
        for (LabResultBriefVO r : ev.getLabResults()) {
            sb.append(safe(r.getLaboratoryItemName())).append(' ')
                    .append(safe(r.getResultValue())).append(' ')
                    .append(safe(r.getAbnormalDesc())).append(' ')
                    .append(safe(r.getJudgeNote())).append(' ');
        }
        for (LaboratoryRecordBriefVO r : ev.getLabRecords()) {
            sb.append(safe(r.getLaboratoryItemName())).append(' ')
                    .append(safe(r.getDiagnosis())).append(' ')
                    .append(safe(r.getSuggestions())).append(' ');
        }
        for (InspectionRecordBriefVO r : ev.getInspections()) {
            sb.append(safe(r.getInspectionItemName())).append(' ')
                    .append(safe(r.getClinicalDiagnosis())).append(' ')
                    .append(safe(r.getResultConclusion())).append(' ');
        }
        return sb.toString().trim();
    }
}

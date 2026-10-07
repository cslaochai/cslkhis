package com.his.charge.support;

import com.his.charge.entity.BizSettlementDiagnosis;
import com.his.charge.enums.RuleCatalogEnum;
import com.his.common.enums.SysGenderEnum;
import com.his.common.util.TextUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * B 组：逻辑排他。
 *
 * <p>这一组不依赖病历，只看编码自身的内部矛盾 —— 男患者编妊娠、成人编新生儿黄疸、
 * 同一 ICD 重复上报、入院病情「无」却当主诊断。这类问题**证据要求极低、误报率极低**，
 * 所以在医保飞检里属于一查一个准的项目，也最适合做结算前自动拦截。</p>
 */
@Component
public class LogicExclusionRule implements ComplianceRule {

    /**
     * 仅见于男性患者的诊断关键词
     */
    private static final String[] MALE_ONLY = {
            "前列腺", "睾丸", "精囊", "包皮", "阴茎", "精索", "附睾"
    };
    /**
     * 仅见于女性患者的诊断关键词
     */
    private static final String[] FEMALE_ONLY = {
            "妊娠", "分娩", "子宫", "卵巢", "月经", "宫颈", "阴道", "输卵管", "前置胎盘", "胎膜", "产褥"
    };
    /**
     * 仅见于新生儿/婴幼儿的诊断关键词（0-1 岁）
     */
    private static final String[] NEONATE_ONLY = {
            "新生儿", "早产儿", "胎粪", "围产期", "先天性", "胎儿"
    };
    /**
     * 仅见于老年患者的诊断关键词（≥60 岁）
     */
    private static final String[] ELDERLY_ONLY = {
            "老年性", "阿尔茨海默", "老年痴呆"
    };

    private static final int NEONATE_MAX_AGE = 1;
    private static final int ELDERLY_MIN_AGE = 60;

    @Override
    public String group() {
        return "B";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext ctx) {
        List<RuleFinding> findings = new ArrayList<>();
        evaluateB01(ctx, findings);
        evaluateB02(ctx, findings);
        evaluateB03(ctx, findings);
        evaluateB04(ctx, findings);
        evaluateB05(ctx, findings);
        return findings;
    }

    /**
     * B01 诊断与患者性别矛盾
     */
    private void evaluateB01(RuleContext ctx, List<RuleFinding> findings) {
        if (ctx.getDiagnoses().isEmpty()) {
            findings.add(RuleFinding.na(RuleCatalogEnum.B01, "清单无诊断明细，无需评估"));
            return;
        }
        Integer gender = ctx.getEvidence().gender();
        SysGenderEnum g = SysGenderEnum.fromCode(gender);
        if (gender == null) {
            findings.add(RuleFinding.na(RuleCatalogEnum.B01,
                    "患者性别未知（档案与清单均未填），无法做性别排他判定"));
            return;
        }
        for (BizSettlementDiagnosis d : ctx.getDiagnoses()) {
            String name = d.getIcdName();
            if (!TextUtil.hasText(name)) {
                findings.add(RuleFinding.na(RuleCatalogEnum.B01, "诊断名称为空，无法做性别排他判定")
                        .on(1, d.getId(), d.getIcdCode(), d.getIcdName()));
                continue;
            }
            String conflict = null;
            if (g == SysGenderEnum.MALE && EvidenceKeywordMatcher.hits(name, FEMALE_ONLY)) {
                conflict = "该诊断仅见于女性";
            } else if (g == SysGenderEnum.FEMALE && EvidenceKeywordMatcher.hits(name, MALE_ONLY)) {
                conflict = "该诊断仅见于男性";
            }
            if (conflict != null) {
                findings.add(RuleFinding.hit(RuleCatalogEnum.B01,
                                "患者性别为" + g.getLabel() + "，但诊断「" + name + "」"
                                        + conflict)
                        .on(1, d.getId(), d.getIcdCode(), d.getIcdName()));
            } else {
                findings.add(RuleFinding.pass(RuleCatalogEnum.B01, "诊断「" + name + "」与性别无冲突")
                        .on(1, d.getId(), d.getIcdCode(), d.getIcdName()));
            }
        }
    }

    /**
     * B02 诊断与患者年龄矛盾
     */
    private void evaluateB02(RuleContext ctx, List<RuleFinding> findings) {
        if (ctx.getDiagnoses().isEmpty()) {
            findings.add(RuleFinding.na(RuleCatalogEnum.B02, "清单无诊断明细，无需评估"));
            return;
        }
        Integer age = ctx.getEvidence().age();
        if (age == null) {
            findings.add(RuleFinding.na(RuleCatalogEnum.B02,
                    "患者年龄未知（档案与清单均未填），无法做年龄排他判定"));
            return;
        }
        for (BizSettlementDiagnosis d : ctx.getDiagnoses()) {
            String name = d.getIcdName();
            if (!TextUtil.hasText(name)) {
                findings.add(RuleFinding.na(RuleCatalogEnum.B02, "诊断名称为空，无法做年龄排他判定")
                        .on(1, d.getId(), d.getIcdCode(), d.getIcdName()));
                continue;
            }
            String conflict = null;
            if (age > NEONATE_MAX_AGE && EvidenceKeywordMatcher.hits(name, NEONATE_ONLY)) {
                conflict = "该诊断仅见于新生儿/婴幼儿（≤" + NEONATE_MAX_AGE + "岁）";
            } else if (age < ELDERLY_MIN_AGE && EvidenceKeywordMatcher.hits(name, ELDERLY_ONLY)) {
                conflict = "该诊断仅见于老年患者（≥" + ELDERLY_MIN_AGE + "岁）";
            }
            if (conflict != null) {
                findings.add(RuleFinding.hit(RuleCatalogEnum.B02,
                                "患者年龄 " + age + " 岁，但诊断「" + name + "」" + conflict)
                        .on(1, d.getId(), d.getIcdCode(), d.getIcdName()));
            } else {
                findings.add(RuleFinding.pass(RuleCatalogEnum.B02, "诊断「" + name + "」与年龄无冲突")
                        .on(1, d.getId(), d.getIcdCode(), d.getIcdName()));
            }
        }
    }

    /**
     * B03 诊断重复编码
     */
    private void evaluateB03(RuleContext ctx, List<RuleFinding> findings) {
        if (ctx.getDiagnoses().isEmpty()) {
            findings.add(RuleFinding.na(RuleCatalogEnum.B03, "清单无诊断明细，无需评估"));
            return;
        }
        Map<String, Integer> counter = new HashMap<>();
        for (BizSettlementDiagnosis d : ctx.getDiagnoses()) {
            if (TextUtil.hasText(d.getIcdCode())) {
                counter.merge(d.getIcdCode().toUpperCase(), 1, Integer::sum);
            }
        }
        if (counter.isEmpty()) {
            findings.add(RuleFinding.na(RuleCatalogEnum.B03, "清单诊断均未编码，无法查重"));
            return;
        }
        List<String> dups = new ArrayList<>();
        for (Map.Entry<String, Integer> e : counter.entrySet()) {
            if (e.getValue() > 1) {
                dups.add(e.getKey() + "×" + e.getValue());
            }
        }
        if (dups.isEmpty()) {
            findings.add(RuleFinding.pass(RuleCatalogEnum.B03, "诊断编码无重复（共 " + counter.size() + " 个不同编码）"));
        } else {
            findings.add(RuleFinding.hit(RuleCatalogEnum.B03, "诊断编码重复上报：" + String.join("、", dups)));
        }
    }

    /**
     * B04 主要诊断入院病情为「无」
     */
    private void evaluateB04(RuleContext ctx, List<RuleFinding> findings) {
        BizSettlementDiagnosis main = ctx.mainDiagnosis();
        if (main == null) {
            findings.add(RuleFinding.na(RuleCatalogEnum.B04, "清单无诊断明细，无需评估"));
            return;
        }
        Integer cond = main.getAdmitCondition();
        if (cond == null) {
            findings.add(RuleFinding.na(RuleCatalogEnum.B04,
                            "主诊断未填入院病情，无法评估（入院病情是主诊断选择的核心依据）")
                    .on(1, main.getId(), main.getIcdCode(), main.getIcdName()));
            return;
        }
        if (cond == 4) {
            findings.add(RuleFinding.hit(RuleCatalogEnum.B04,
                            "主要诊断「" + TextUtil.nullToEmpty(main.getIcdName()) + "」入院病情为「无」，"
                                    + "表示入院时并不存在该情况，不能作为主要诊断")
                    .on(1, main.getId(), main.getIcdCode(), main.getIcdName()));
            return;
        }
        findings.add(RuleFinding.pass(RuleCatalogEnum.B04, "主诊断入院病情为 " + cond + "，非「无」")
                .on(1, main.getId(), main.getIcdCode(), main.getIcdName()));
    }

    /**
     * B05 标注 CC/MCC 但无对应并发症诊断
     */
    private void evaluateB05(RuleContext ctx, List<RuleFinding> findings) {
        List<BizSettlementDiagnosis> ccDiags = new ArrayList<>();
        for (BizSettlementDiagnosis d : ctx.getDiagnoses()) {
            String level = d.getCcLevel();
            if (TextUtil.hasText(level) && ("CC".equalsIgnoreCase(level) || "MCC".equalsIgnoreCase(level))) {
                ccDiags.add(d);
            }
        }
        if (ccDiags.isEmpty()) {
            findings.add(RuleFinding.na(RuleCatalogEnum.B05, "清单未标注 CC/MCC 级别，无需评估升级依据"));
            return;
        }
        long otherCount = ctx.getDiagnoses().stream()
                .filter(d -> d.getDiagType() != null && d.getDiagType() == 2)
                .count();
        if (otherCount == 0) {
            for (BizSettlementDiagnosis d : ccDiags) {
                findings.add(RuleFinding.hit(RuleCatalogEnum.B05,
                                "诊断「" + TextUtil.nullToEmpty(d.getIcdName()) + "」标注为 " + d.getCcLevel()
                                        + "（会提升病组权重），但清单无任何其他诊断作为并发症/合并症依据")
                        .on(1, d.getId(), d.getIcdCode(), d.getIcdName()));
            }
            return;
        }
        for (BizSettlementDiagnosis d : ccDiags) {
            findings.add(RuleFinding.pass(RuleCatalogEnum.B05,
                            "标注 " + d.getCcLevel() + "，清单含 " + otherCount + " 条其他诊断可作为依据")
                    .on(1, d.getId(), d.getIcdCode(), d.getIcdName()));
        }
    }
}

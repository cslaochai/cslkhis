package com.his.charge.support;

import com.his.common.enums.DipDiseaseTypeEnum;
import com.his.common.util.TextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DIP 入组：主诊断 + 手术操作组合匹配病种（平铺模型，与 DRG 的 MDC→ADRG→DRG 层级两跳完全不同）。
 *
 * <p>判定顺序（官方 DIP 规则）：
 * <ol>
 *   <li>核心病种：主诊断整码 + 手术操作整码精确匹配（按目录行逐个操作比对，目录内手术病种优先）；</li>
 *   <li>核心病种：主诊断整码 + 目录行为「无手术操作」（内科/保守治疗病种），仅当病例无主手术时才取；</li>
 *   <li>综合病种：主诊断前缀归组（最长前缀优先，装载时已排序）。</li>
 * </ol>
 * 以上均不命中才判未入组，诚实返回 QY，不编造病种码。
 *
 * <p>辅助目录：命中病种后按 病种专属 → 全局 的顺序匹配规则，年龄型按周岁区间、码型按其他诊断整码，
 * 首条命中即取该系数；未命中系数为 1.0000（不变分值）。
 */
@Component
@RequiredArgsConstructor
public class DipGrouper {

    /**
     * 未入组的本地结果码
     */
    private static final String QY_CODE = "QY";

    /**
     * 结果说明列宽，超出即截断
     */
    private static final int NOTE_MAX = 300;

    /**
     * 辅助目录未命中时的系数（不变分值）
     */
    private static final BigDecimal DEFAULT_COEFF = BigDecimal.ONE;

    private final DipSchemeCache dipSchemeCache;

    /**
     * 单条分组结果（纯值对象，避免组表耦合）。
     *
     * <p>{@code baseScore} 可能是 null：病种目录的分值由统筹区下发，官方病种库不含；
     * 落成 0 会让「没分值」看起来像「分值是 0」。coeff 恒非空（未命中辅助目录为 1.0000）。
     */
    public record GroupResult(String dipCode, String dipName, Integer diseaseType,
                              BigDecimal baseScore, BigDecimal coeff, String ruleNote, boolean grouped) {
    }

    public GroupResult group(DipFacts facts, String version) {
        if (facts.mainDiag().isEmpty()) {
            return qy("主诊断编码为空，无法入组");
        }
        DipScheme scheme = dipSchemeCache.get(version);
        if (scheme.empty()) {
            return qy("未接入 DIP 病种库（版本 " + (version == null ? "" : version) + " 的病种目录为空），无法入组");
        }

        // 1) 核心病种：主诊断整码 + 手术操作整码精确匹配（目录内手术病种优先）
        for (DipScheme.DiseaseNode core : scheme.coreOf(facts.mainDiag())) {
            if (!TextUtil.hasText(core.operCode())) {
                continue;
            }
            for (String oper : facts.mainOpers()) {
                if (core.operCode().equals(oper)) {
                    return hit(scheme, facts, core);
                }
            }
        }
        // 2) 核心病种：主诊断整码 + 无手术操作病种，仅当病例无主手术时才取
        if (facts.mainOpers().isEmpty()) {
            for (DipScheme.DiseaseNode core : scheme.coreOf(facts.mainDiag())) {
                if (!TextUtil.hasText(core.operCode())) {
                    return hit(scheme, facts, core);
                }
            }
        }
        // 3) 综合病种：主诊断前缀归组（最长前缀优先）
        DipScheme.DiseaseNode comp = scheme.comprehensiveMatch(facts.mainDiag());
        if (comp != null) {
            return hit(scheme, facts, comp);
        }
        return qy("主诊断 " + facts.mainDiag() + " 未命中核心病种目录，也无综合病种可归组"
                + operHint(facts) + "（编码不在病种库内或方案未覆盖），判为未入组");
    }

    private GroupResult hit(DipScheme scheme, DipFacts facts, DipScheme.DiseaseNode node) {
        BigDecimal coeff = auxCoeff(scheme, facts, node);
        DipDiseaseTypeEnum type = DipDiseaseTypeEnum.fromCode(node.diseaseType());
        StringBuilder note = new StringBuilder()
                .append("主诊断 ").append(facts.mainDiag())
                .append(operHint(facts))
                .append(" → ").append(type == null ? "病种" : type.getLabel())
                .append(" ").append(label(node.diseaseCode(), node.diseaseName()))
                .append(auxHint(coeff));
        if (node.baseScore() == null) {
            note.append("；基准分值由统筹区医保局下发，当前未接入");
        }
        return new GroupResult(node.diseaseCode(), node.diseaseName(), node.diseaseType(),
                node.baseScore(), coeff, TextUtil.cut(note.toString(), NOTE_MAX), true);
    }

    /**
     * 辅助目录系数：病种专属规则在前、全局规则在后，首条命中即取。
     */
    private BigDecimal auxCoeff(DipScheme scheme, DipFacts facts, DipScheme.DiseaseNode node) {
        Integer ageYears = facts.ageYears();
        List<DipScheme.AuxRule> rules = new ArrayList<>(scheme.auxOf(node.diseaseCode()));
        for (DipScheme.AuxRule r : rules) {
            if (r.auxType() != null && r.auxType() == 1) {
                // 年龄型：周岁落在 [ageMin, ageMax] 闭区间
                if (ageYears != null && r.ageMin() != null && r.ageMax() != null
                        && ageYears >= r.ageMin() && ageYears <= r.ageMax()) {
                    return r.coeff();
                }
            } else {
                // 码型（合并症并发症 / 肿瘤分级等）：其他诊断整码命中即取
                if (r.auxCode() != null && !r.auxCode().isEmpty() && facts.otherDiags().contains(r.auxCode())) {
                    return r.coeff();
                }
            }
        }
        return DEFAULT_COEFF;
    }

    private String operHint(DipFacts facts) {
        return facts.mainOpers().isEmpty() ? "" : " + 主手术 " + String.join("、", facts.mainOpers());
    }

    private String auxHint(BigDecimal coeff) {
        return coeff.compareTo(DEFAULT_COEFF) == 0 ? ""
                : "（辅助目录命中，病例组合系数 " + coeff.stripTrailingZeros().toPlainString() + "）";
    }

    private String label(String code, String name) {
        return TextUtil.hasText(name) ? code + " " + name : code;
    }

    private GroupResult qy(String reason) {
        return new GroupResult(QY_CODE, null, null, null, DEFAULT_COEFF,
                TextUtil.cut(reason, NOTE_MAX), false);
    }
}

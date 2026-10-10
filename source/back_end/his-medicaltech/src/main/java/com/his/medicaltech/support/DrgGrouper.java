package com.his.medicaltech.support;

import com.his.common.util.TextUtil;
import com.his.medicaltech.enums.DrgCcLevelEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * DRG 入组：按官方三级目录逐层判定（MDC → ADRG → DRG 细分组）。
 *
 * <p>每层的判定依据只有该层目录行的规则原文（{@link DrgRuleParser} 编译成表达式），
 * 规则里的集合编号展开成精确 ICD 码后整码比较（{@link DrgScheme}），不做前缀近似 ——
 * 前缀匹配会把「J18」与「J18.9」当成同一个病，而官方目录把它们放在不同集合里。
 *
 * <p><b>空规则行是「这一档不带条件」，三层各有各的含义，一律显式排到最后才取</b>：
 * MDC 层是先期分组（能不能进由它子 ADRG 的规则决定，本身不作门槛）；
 * ADRG 层是该 MDC 的末组（官方把「本组剩余病例」写成了空规则行，名字也是空的）；
 * DRG 层是该 ADRG 的兜底档（「不伴合并症或并发症」）。
 *
 * <p>只有主诊断落不进任何 MDC 才是未入组，此时诚实返回 QY，不编造组号。
 */
@Component
@RequiredArgsConstructor
public class DrgGrouper {

    /**
     * 未入组的本地结果码：官方目录用一串 0 的分隔行表达同一含义，装载时已判掉，不入库
     */
    private static final String QY_CODE = "QY";

    /**
     * 结果说明列宽，超出即截断（说明里带三级目录的中文名，长病案会顶到上限）
     */
    private static final int NOTE_MAX = 300;

    private final DrgSchemeCache drgSchemeCache;

    /**
     * 单条分组结果（纯值对象，避免组表耦合）。
     *
     * <p>{@code weight}/{@code payStandard} 可能是 null：官方包只下发目录结构，
     * 权重与支付标准由统筹区医保局另行制定，落成 0 会让「没标准」看起来像「标准是 0 元」。
     */
    public record GroupResult(String drgCode, String drgName, String mdcCode,
                              BigDecimal weight, BigDecimal payStandard, String ruleNote, boolean grouped) {
    }

    public GroupResult group(DrgFacts facts) {
        if (facts.mainDiag().isEmpty()) {
            return qy("主诊断编码为空，无法入组");
        }
        DrgScheme scheme = drgSchemeCache.get();
        if (scheme.empty()) {
            return qy("未接入官方分组方案（分组目录为空），无法入组");
        }

        String enteredMdc = null;
        for (DrgScheme.MdcNode mdc : scheme.mdcs()) {
            if (mdc.rule() == null) {
                // 先期分组本身不作门槛，能不能进由它子 ADRG 的规则决定，所以也不算「命中了这个 MDC」
                GroupResult hit = matchAdrg(scheme, facts, mdc);
                if (hit != null) {
                    return hit;
                }
                continue;
            }
            if (!mdc.rule().matches(facts, scheme)) {
                continue;
            }
            enteredMdc = mdc.code();
            GroupResult hit = matchAdrg(scheme, facts, mdc);
            if (hit != null) {
                return hit;
            }
        }
        return qy(enteredMdc == null
                ? "主诊断 " + facts.mainDiag() + " 未命中任何 MDC 规则（编码不在目录内或方案未覆盖），判为未入组"
                : "命中 MDC " + enteredMdc + "，但其下 ADRG 规则均未匹配，且该 MDC 没有末组：主诊断 "
                        + facts.mainDiag() + operHint(facts));
    }

    /**
     * 一个 MDC 之内：带规则的 ADRG 按目录顺序首条命中；该 MDC 的末组（空规则 ADRG）只在其余全不命中后才取。
     */
    private GroupResult matchAdrg(DrgScheme scheme, DrgFacts facts, DrgScheme.MdcNode mdc) {
        DrgScheme.AdrgNode tail = null;
        for (DrgScheme.AdrgNode adrg : scheme.adrgsOf(mdc.code())) {
            if (adrg.rule() == null) {
                if (tail == null) {
                    tail = adrg;
                }
                continue;
            }
            if (!adrg.rule().matches(facts, scheme)) {
                continue;
            }
            GroupResult hit = matchDrg(scheme, facts, mdc, adrg, true);
            if (hit != null) {
                return hit;
            }
        }
        return tail == null ? null : matchDrg(scheme, facts, mdc, tail, false);
    }

    /**
     * ADRG 下取细分组：带规则的按目录顺序首条命中，全不命中才落兜底档。
     *
     * <p>兜底档不靠「排在最后」隐式生效而是显式留到最后一步 —— 官方表把「不伴合并症或并发症」
     * 写成了空规则行，一旦某统筹区重排了行序，按顺序取第一条空规则就会把伴 MCC 的病例错入低权重组。
     */
    private GroupResult matchDrg(DrgScheme scheme, DrgFacts facts,
                                 DrgScheme.MdcNode mdc, DrgScheme.AdrgNode adrg, boolean adrgByRule) {
        DrgScheme.DrgNode fallback = null;
        for (DrgScheme.DrgNode drg : scheme.drgsOf(adrg.code())) {
            if (drg.rule() == null) {
                if (fallback == null) {
                    fallback = drg;
                }
                continue;
            }
            if (drg.rule().matches(facts, scheme)) {
                return hit(facts, scheme, mdc, adrg, drg, true, adrgByRule);
            }
        }
        return fallback == null ? null : hit(facts, scheme, mdc, adrg, fallback, false, adrgByRule);
    }

    private GroupResult hit(DrgFacts facts, DrgScheme scheme, DrgScheme.MdcNode mdc,
                            DrgScheme.AdrgNode adrg, DrgScheme.DrgNode drg,
                            boolean byRule, boolean adrgByRule) {
        StringBuilder note = new StringBuilder()
                .append("主诊断 ").append(facts.mainDiag())
                .append(operHint(facts))
                .append(" → MDC ").append(label(mdc.code(), mdc.name()))
                .append(" → ADRG ").append(label(adrg.code(), adrg.name()))
                .append(" → DRG ").append(label(drg.code(), drg.name()))
                .append(ccHint(facts, scheme));
        if (!adrgByRule) {
            note.append("（本 MDC 末组：目录里这一档不带条件）");
        } else if (!byRule) {
            note.append("（兜底档：该 ADRG 下无子组规则命中）");
        }
        if (drg.payStandard() == null) {
            note.append("；权重与支付标准由统筹区医保局下发，当前未接入");
        }
        return new GroupResult(drg.code(), drg.name(), mdc.code(), drg.weight(), drg.payStandard(),
                TextUtil.cut(note.toString(), NOTE_MAX), true);
    }

    /**
     * 官方表给「其他手术」歧义档与末组留了空名字，只报编码，不把 null 拼进说明里
     */
    private String label(String code, String name) {
        return DrgScheme.named(name) ? code + " " + name : code;
    }

    private String operHint(DrgFacts facts) {
        return facts.mainOpers().isEmpty() ? "" : " + 主手术 " + String.join("、", facts.mainOpers());
    }

    /**
     * 标出把病例推向高权重组的那条其他诊断（编码员核对高编高套时只看这一句就够）
     */
    private String ccHint(DrgFacts facts, DrgScheme scheme) {
        String code = null;
        DrgCcLevelEnum max = DrgCcLevelEnum.NONE;
        for (String other : facts.otherDiags()) {
            DrgCcLevelEnum level = DrgCcLevelEnum.max(max, scheme.ccLevel(other, facts.mainDiag()));
            if (level != max) {
                max = level;
                code = other;
            }
        }
        return code == null ? "" : "（其他诊断 " + code + " 判为" + max.getLabel() + "）";
    }

    private GroupResult qy(String reason) {
        return new GroupResult(QY_CODE, null, null, null, null, TextUtil.cut(reason, NOTE_MAX), false);
    }
}

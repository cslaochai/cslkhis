package com.his.charge.support;

import com.his.common.enums.DrgCcLevelEnum;
import com.his.common.util.TextUtil;
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
 * <p><b>行类型决定结果语义，不能再靠「名字为空」猜</b>：
 * <li>{@link DrgScheme#ROW_QY}：官方 ?QY 其他手术歧义组，返回该 ADRG 编码（如 BQY），标记歧义；</li>
 * <li>{@link DrgScheme#ROW_PLACEHOLDER}：官方 ?00 未入组占位，绝不能返回 B000 之类占位码，统一返回 0000；</li>
 * <li>{@link DrgScheme#ROW_NORMAL}：正常病组；空规则行分别是 MDC 先期分组、ADRG 末组、DRG 兜底档。</li>
 *
 * <p>只有主诊断落不进任何 MDC 或落进 MDC 但子组全不命中才是未入组，返回 0000，不编造组号。
 */
@Component
@RequiredArgsConstructor
public class DrgGrouper {

    /**
     * 未入组结果码：官方口径的 0000（原实现返回 QY，把「未入组」与「歧义」混为一码）
     */
    public static final String UNGROUPED_CODE = "0000";

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
     *
     * @param grouped   是否入组（歧义组与未入组都是 false）
     * @param ambiguous 是否为官方 ?QY 歧义组（是合法结果码，不计权重与盈亏）
     */
    public record GroupResult(String drgCode, String drgName, String mdcCode,
                              BigDecimal weight, BigDecimal payStandard, String ruleNote,
                              boolean grouped, boolean ambiguous) {
    }

    public GroupResult group(DrgFacts facts, String version) {
        if (facts.mainDiag().isEmpty()) {
            return ungrouped("主诊断编码为空，无法入组");
        }
        DrgScheme scheme = drgSchemeCache.get(version);
        if (scheme.empty()) {
            return ungrouped("未接入官方分组方案（版本 " + (version == null ? "" : version) + " 的分组目录为空），无法入组");
        }

        String enteredMdc = null;
        // 目录已按 group_step、sort_no 排好：先期分组在前，普通 MDC 在后
        for (DrgScheme.MdcNode mdc : scheme.mdcs()) {
            if (mdc.rule() == null) {
                // 只有显式标记为先期分组的空规则 MDC（如 MDCA）才不作门槛；
                // 普通 MDC 缺规则时若放行，等于「任何病例都先进这个 MDC」
                if (!mdc.preGroup()) {
                    continue;
                }
                GroupResult hit = matchAdrg(scheme, facts, mdc);
                if (hit != null) {
                    return hit;
                }
                continue;
            }
            if (!mdc.rule().matches(facts, scheme)) {
                continue;
            }
            GroupResult hit = matchAdrg(scheme, facts, mdc);
            if (hit != null) {
                return hit;
            }
            // 先期分组一旦判定成立就不许回落：多发伤病例若在本 MDC 下没有可用末组，
            // 继续往后遍历会把它错入到后面普通 MDC 的高权重组（如颅脑手术组），这比判未入组更危险。
            if (mdc.preGroup()) {
                return ungrouped("主诊断 " + facts.mainDiag() + operHint(facts) + " → 命中先期分组 MDC "
                        + label(mdc.code(), mdc.name()) + "，但其下 ADRG 规则均未匹配且没有可用末组/占位：按先期分组优先口径判为未入组");
            }
            enteredMdc = mdc.code();
        }
        return ungrouped(enteredMdc == null
                ? "主诊断 " + facts.mainDiag() + " 未命中任何 MDC 规则（编码不在目录内或方案未覆盖），判为未入组"
                : "命中 MDC " + enteredMdc + "，但其下 ADRG 规则均未匹配，且该 MDC 没有末组：主诊断 "
                        + facts.mainDiag() + operHint(facts));
    }

    /**
     * 一个 MDC 之内：带规则的正常 ADRG 按目录顺序首条命中；QY 歧义组按规则命中即返回歧义；
     * 官方 ?00 占位只在其余全不命中后才生效，统一返回 0000。
     */
    private GroupResult matchAdrg(DrgScheme scheme, DrgFacts facts, DrgScheme.MdcNode mdc) {
        DrgScheme.AdrgNode tail = null;
        for (DrgScheme.AdrgNode adrg : scheme.adrgsOf(mdc.code())) {
            int kind = kindOf(adrg.rowKind());
            if (kind == DrgScheme.ROW_QY) {
                if (adrg.rule() == null || adrg.rule().matches(facts, scheme)) {
                    return ambiguous(facts, scheme, mdc, adrg);
                }
                continue;
            }
            if (kind == DrgScheme.ROW_PLACEHOLDER) {
                if (tail == null) {
                    tail = adrg;
                }
                continue;
            }
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
        if (tail == null) {
            return null;
        }
        if (kindOf(tail.rowKind()) == DrgScheme.ROW_PLACEHOLDER) {
            return ungrouped("主诊断 " + facts.mainDiag() + operHint(facts) + " → MDC " + mdc.code()
                    + " → 命中官方未入组占位 " + tail.code() + "（该 MDC 其余组均不匹配），按口径返回 "
                    + UNGROUPED_CODE);
        }
        return matchDrg(scheme, facts, mdc, tail, false);
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
        DrgScheme.DrgNode placeholder = null;
        for (DrgScheme.DrgNode drg : scheme.drgsOf(adrg.code())) {
            int kind = kindOf(drg.rowKind());
            if (kind == DrgScheme.ROW_QY) {
                if (drg.rule() == null || drg.rule().matches(facts, scheme)) {
                    return ambiguousDrg(facts, scheme, mdc, adrg, drg);
                }
                continue;
            }
            if (kind == DrgScheme.ROW_PLACEHOLDER) {
                if (placeholder == null) {
                    placeholder = drg;
                }
                continue;
            }
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
        if (placeholder != null) {
            return ungrouped("主诊断 " + facts.mainDiag() + operHint(facts) + " → MDC " + mdc.code()
                    + " → ADRG " + adrg.code() + " → 命中未入组占位 " + placeholder.code()
                    + "，按口径返回 " + UNGROUPED_CODE);
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
                TextUtil.cut(note.toString(), NOTE_MAX), true, false);
    }

    /**
     * 官方 ?QY：其他手术歧义组。编码本身是合法结果码，但不入权重、不算盈亏。
     */
    private GroupResult ambiguous(DrgFacts facts, DrgScheme scheme, DrgScheme.MdcNode mdc, DrgScheme.AdrgNode adrg) {
        return new GroupResult(adrg.code(), null, mdc.code(), null, null,
                TextUtil.cut("主诊断 " + facts.mainDiag() + operHint(facts)
                        + " → MDC " + label(mdc.code(), mdc.name())
                        + " → 命中 " + adrg.code() + "（官方其他手术歧义组）"
                        + "，结果为合法歧义码，不计权重与盈亏", NOTE_MAX), false, true);
    }

    private GroupResult ambiguousDrg(DrgFacts facts, DrgScheme scheme, DrgScheme.MdcNode mdc,
                                     DrgScheme.AdrgNode adrg, DrgScheme.DrgNode drg) {
        return new GroupResult(drg.code(), null, mdc.code(), null, null,
                TextUtil.cut("主诊断 " + facts.mainDiag() + operHint(facts)
                        + " → MDC " + label(mdc.code(), mdc.name())
                        + " → ADRG " + adrg.code()
                        + " → 命中 " + drg.code() + "（官方歧义档），不计权重与盈亏", NOTE_MAX), false, true);
    }

    private static int kindOf(Integer rowKind) {
        return rowKind == null ? DrgScheme.ROW_NORMAL : rowKind;
    }

    /**
     * 官方表给歧义档与占位行留了空名字，只报编码，不把 null 拼进说明里
     */
    private String label(String code, String name) {
        return TextUtil.hasText(name) ? code + " " + name : code;
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

    private GroupResult ungrouped(String reason) {
        return new GroupResult(UNGROUPED_CODE, null, null, null, null,
                TextUtil.cut(reason, NOTE_MAX), false, false);
    }
}

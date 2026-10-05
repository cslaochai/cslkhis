package com.his.charge.support;

import com.his.charge.config.ComplianceProperties;
import com.his.charge.entity.SysDrgGroup;
import com.his.charge.enums.RuleCatalogEnum;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * D 组：分组倍率。
 *
 * <p>费用倍率 = 实际总费用 / 该病组支付标准。倍率异常同时指向两个方向：
 * 倍率过高 → 编了高权重病组（高编高套）；倍率过低 → 编了低权重病组（低编入组）。
 * 所以这一组是「高编」和「低编」唯一能同框出现的规则。</p>
 *
 * <p><b>硬约束</b>：支付标准来自医保局下发的分组方案。分组表为空时本组一律返回「不适用」，
 * 绝不硬编码权重估算 —— 权重错一位，整个倍率结论就是错的，而错的倍率拿去申诉会被直接驳回。</p>
 */
@Component
public class GroupingRatioRule implements ComplianceRule {

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    @Override
    public String group() {
        return "D";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext ctx) {
        List<RuleFinding> findings = new ArrayList<>();
        evaluateD01(ctx, findings);
        evaluateD02(ctx, findings);
        return findings;
    }

    /**
     * D01 费用倍率异常
     */
    private void evaluateD01(RuleContext ctx, List<RuleFinding> findings) {
        if (!ctx.isDrgTableReady()) {
            findings.add(RuleFinding.na(RuleCatalogEnum.D01,
                    "本地未接入医保 DRG/DIP 分组方案（sys_drg_group 为空），无病组支付标准，无法计算费用倍率"));
            return;
        }
        SysDrgGroup group = ctx.getDrgGroup();
        if (group == null) {
            findings.add(RuleFinding.na(RuleCatalogEnum.D01, "未匹配到该清单的 DRG 分组，无法取得支付标准"));
            return;
        }
        BigDecimal payStandard = group.getPayStandard();
        if (payStandard == null || payStandard.compareTo(BigDecimal.ZERO) <= 0) {
            findings.add(RuleFinding.na(RuleCatalogEnum.D01,
                    "分组 " + group.getDrgCode() + " 未配置支付标准，无法计算费用倍率"));
            return;
        }
        BigDecimal actual = ctx.getEvidence().actualCost();
        if (actual == null || actual.compareTo(BigDecimal.ZERO) <= 0) {
            findings.add(RuleFinding.na(RuleCatalogEnum.D01, "实际总费用为 0 或缺失，无法计算费用倍率"));
            return;
        }

        ComplianceProperties props = ctx.getProperties();
        BigDecimal high = props == null || props.getHighCostRatio() == null
                ? new BigDecimal("2.0") : props.getHighCostRatio();
        BigDecimal low = props == null || props.getLowCostRatio() == null
                ? new BigDecimal("0.5") : props.getLowCostRatio();

        BigDecimal ratio = actual.divide(payStandard, 4, RoundingMode.HALF_UP);

        if (ratio.compareTo(high) > 0) {
            findings.add(RuleFinding.hit(RuleCatalogEnum.D01,
                            "费用倍率 " + ratio + " 超过高倍率阈值 " + high
                                    + "（实际 " + actual + " 元 / 支付标准 " + payStandard + " 元），指向高编高套")
                    .withSuggestion("核对是否编入了高权重病组或虚增费用；确认无误需准备申诉材料"));
            return;
        }
        if (ratio.compareTo(low) < 0) {
            findings.add(RuleFinding.hit(RuleCatalogEnum.D01,
                            "费用倍率 " + ratio + " 低于低倍率阈值 " + low
                                    + "（实际 " + actual + " 元 / 支付标准 " + payStandard + " 元），指向低编入组")
                    .withSuggestion("核对是否漏编严重并发症（CC/MCC）或压低主诊断，导致入到低权重组"));
            return;
        }
        findings.add(RuleFinding.pass(RuleCatalogEnum.D01,
                "费用倍率 " + ratio + " 在合理区间 [" + low + ", " + high + "] 内"));
    }

    /**
     * D02 未接入DRG分组方案 / 未入组
     */
    private void evaluateD02(RuleContext ctx, List<RuleFinding> findings) {
        if (!ctx.isDrgTableReady()) {
            findings.add(RuleFinding.na(RuleCatalogEnum.D02,
                            "本地未接入医保 DRG/DIP 分组方案，无法评估入组结果")
                    .withSuggestion("从医保局获取当地 CHS-DRG/DIP 分组方案，导入 sys_drg_group 后即可启用 D 组规则"));
            return;
        }
        String drgCode = ctx.getSettlement() == null ? null : ctx.getSettlement().getDrgCode();
        if (!StringUtils.hasText(drgCode)) {
            findings.add(RuleFinding.hit(RuleCatalogEnum.D02,
                    "分组方案已接入，但该清单未填 DRG 分组编码（未入组）"));
            return;
        }
        if (ctx.getDrgGroup() == null) {
            findings.add(RuleFinding.hit(RuleCatalogEnum.D02,
                    "清单 DRG 编码 " + drgCode + " 在分组方案中不存在，可能使用了过期或不存在的分组"));
            return;
        }
        findings.add(RuleFinding.pass(RuleCatalogEnum.D02,
                "已入组且分组有效：" + drgCode + " " + safe(ctx.getDrgGroup().getDrgName())));
    }
}

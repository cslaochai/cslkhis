package com.his.charge.support;

import com.his.charge.mapper.DrgSimMapper;
import com.his.common.util.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 未入组原因补充说明：把「编码口径不对」与「首页维度缺失」写进 rule_note。
 *
 * <p>入组判定只有 {@link DrgGrouper} 一处，本类不做判定、不改结果，只在未入组时补一句能直接落地的提示。
 * 原因很实际：原来的说明只有「编码不在目录内或方案未覆盖」，编码员看到这句时分不清是自己选了历史缩写码，
 * 还是首页性别/年龄没填 —— 两件事的处理人和处理动作完全不同。
 *
 * <p><b>编码只标口径，绝不自动改码</b>：院内历史缩写码与医保版贯标码是两套编号体系，反向归一有歧义
 * （现字典 J18.001 既可能是官方 J18.001，也可能是 J18.000x001 的缩写，见 235 增量 SQL 的取舍），
 * 因此这里只提示「该码是历史缩写码」，改由编码员改用贯标码重跑。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DrgGroupDiagnosis {

    /**
     * rule_note 列宽：biz_drg_sim_result 与 biz_medpay_settle 都是 varchar(300)
     */
    private static final int NOTE_MAX = 300;

    /**
     * 补充说明自身上限：留出主体说明的空间，避免把命中路径整句挤掉
     */
    private static final int HINT_MAX = 170;

    private final DrgSimMapper drgSimMapper;

    /**
     * 未入组时在说明末尾补一句原因；已入组 / 歧义组原样返回。
     */
    public String explain(String note, DrgFacts facts, boolean grouped, boolean ambiguous) {
        if (grouped || ambiguous) {
            return note;
        }
        List<String> hints = new ArrayList<>(3);
        // 字典口径只是补充信息，读失败不能让入组结论跟着崩：吞异常留日志，只丢这一条提示
        try {
            String diagHint = caliberHint("主诊断", facts.mainDiag(), drgSimMapper.diagCodeStd(facts.mainDiag()));
            if (diagHint != null) {
                hints.add(diagHint);
            }
            for (String oper : facts.mainOpers()) {
                String operHint = caliberHint("主手术", oper, drgSimMapper.operCodeStd(oper));
                if (operHint != null) {
                    hints.add(operHint);
                    break;
                }
            }
        } catch (Exception e) {
            log.warn("DRG 未入组补充说明读取编码口径失败，本次跳过该提示：{}", e.getMessage());
        }
        List<String> missing = new ArrayList<>(2);
        if (facts.gender() == null) {
            missing.add("性别");
        }
        if (facts.age() == null || facts.ageUnit() == null) {
            missing.add("年龄");
        }
        if (!missing.isEmpty()) {
            hints.add("首页缺少 " + String.join("、", missing) + "，相关规则一律判不命中，补录后重跑");
        }
        if (hints.isEmpty()) {
            return note;
        }
        String hint = TextUtil.cut(String.join("；", hints), HINT_MAX);
        String body = TextUtil.cut(TextUtil.trimToEmpty(note), NOTE_MAX - hint.length() - 1);
        return TextUtil.hasText(body) ? body + "。" + hint : hint;
    }

    /**
     * 单个编码的口径提示；码本身是贯标码或字典里查不到时返回 null（查不到的情况分组器原说明已覆盖）。
     *
     * @param codeStd 字典里同一编码的最大口径标记：1-存在贯标码行 0-只有历史缩写码行 null-字典无此码
     */
    private String caliberHint(String role, String code, Integer codeStd) {
        if (!TextUtil.hasText(code) || codeStd == null || codeStd != 0) {
            return null;
        }
        return role + " " + code + " 是院内历史缩写码，官方分组集合按医保版贯标码收录，请改用贯标码重跑";
    }
}

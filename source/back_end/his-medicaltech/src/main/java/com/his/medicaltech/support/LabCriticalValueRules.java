package com.his.medicaltech.support;

import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import com.his.medicaltech.enums.CriticalTypeEnum;
import com.his.medicaltech.support.LabReferenceRangeParser;

/**
 * 危急值硬规则（纯代码，<b>绝不由模型判断</b>）。
 * <p>
 * 危急值（critical value）是「不及时处置会在短时间内危及生命」的结果，
 * 必须走确定性规则 + 强制通知闭环。让大模型来判断危急值是不可接受的：
 * 漏报一次就是一条命，而模型是概率系统。
 * <p>
 * <b>规则表的两条硬约束</b>：
 * <ol>
 *   <li><b>必须校验单位</b>。同名的「白细胞」在血常规里是 10^9/L（危急值 &lt;1.5），
 *       在尿常规里是「/HP」（0-5 个/高倍视野，完全不同的量纲）。
 *       只按项目名匹配会把「尿白细胞 0 个/HP」报成危急值 —— 这就是典型的告警疲劳来源。</li>
 *   <li><b>阈值取自临床通用标准</b>，不追求覆盖全部项目。覆盖不全只是少报，
 *       阈值定错会乱报；乱报会让人关掉提醒，最终连真危急值也一起被忽略。</li>
 * </ol>
 * 规则表后续建议迁移到检验项目组套明细增加危急值上下限两列，
 * 由检验科自行维护；当前写在代码里是因为库里没有这两列。
 */
public final class LabCriticalValueRules {

    /**
     * 项目规则。编码精确匹配（大写），{@code nameKeyword} 为项目名包含匹配。
     * 二者命中其一即可，但单位必须与结果单位一致。
     */
    private record Rule(String code,
                        String nameKeyword,
                        String label,
                        String unit,
                        Double criticalLow,
                        Double criticalHigh) {
    }

    private static final List<Rule> RULES = List.of(
            new Rule("K", "血钾", "血清钾", "mmol/L", 2.5, 6.0),
            new Rule("NA", "血钠", "血清钠", "mmol/L", 120.0, 160.0),
            new Rule("CA", "血钙", "血清钙", "mmol/L", 1.5, 3.5),
            new Rule("GLU", "血糖", "血糖", "mmol/L", 2.8, 22.2),
            new Rule("HGB", "血红蛋白", "血红蛋白", "g/L", 50.0, 200.0),
            new Rule("PLT", "血小板", "血小板计数", "10^9/L", 30.0, 1000.0),
            new Rule("WBC", "白细胞", "白细胞计数", "10^9/L", 1.5, 30.0),
            new Rule("CR", "肌酐", "肌酐", "umol/L", null, 700.0),
            new Rule("TBIL", "总胆红素", "总胆红素", "umol/L", null, 340.0),
            new Rule("PO2", "氧分压", "动脉血氧分压", "mmHg", 60.0, null),
            new Rule("PCO2", "二氧化碳分压", "动脉血二氧化碳分压", "mmHg", null, 70.0),
            new Rule("CTNI", "肌钙蛋白", "肌钙蛋白", "ng/mL", null, 0.5));

    private LabCriticalValueRules() {
    }

    /**
     * 危急值命中结果
     *
     * @param type          1-偏低 2-偏高
     * @param itemLabel     项目规范名
     * @param value         数值
     * @param unit          单位
     * @param threshold     阈值文本
     * @param description   给医生看的描述
     */
    public record Hit(int type, String itemLabel, double value, String unit,
                      String threshold, String description) {
    }

    /**
     * 判定一条检验结果是否达到危急值。
     *
     * @param itemCode    检验项目编码
     * @param itemName    检验项目名称
     * @param resultUnit  结果单位
     * @param resultValue 结果值
     */
    public static Optional<Hit> check(String itemCode, String itemName, String resultUnit, String resultValue) {
        Double value = LabReferenceRangeParser.firstNumber(resultValue);
        if (value == null) {
            return Optional.empty();
        }
        String unit = normalizeUnit(resultUnit);
        if (!StringUtils.hasText(unit)) {
            // 单位缺失时不做判定：宁可漏报，也不冒按错量纲乱报的风险
            return Optional.empty();
        }
        String code = StringUtils.hasText(itemCode) ? itemCode.trim().toUpperCase(Locale.ROOT) : "";
        String name = StringUtils.hasText(itemName) ? itemName.trim() : "";

        for (Rule rule : RULES) {
            if (!matches(rule, code, name)) {
                continue;
            }
            if (!normalizeUnit(rule.unit()).equals(unit)) {
                continue;
            }
            if (rule.criticalLow() != null && value < rule.criticalLow()) {
                return Optional.of(new Hit(CriticalTypeEnum.LOW.getCode(), rule.label(), value, resultUnit,
                        "低于 " + trim(rule.criticalLow()) + " " + rule.unit(),
                        String.format("%s %s，低于危急值下限 %s %s",
                                rule.label(), format(value), trim(rule.criticalLow()), rule.unit())));
            }
            if (rule.criticalHigh() != null && value > rule.criticalHigh()) {
                return Optional.of(new Hit(CriticalTypeEnum.HIGH.getCode(), rule.label(), value, resultUnit,
                        "高于 " + trim(rule.criticalHigh()) + " " + rule.unit(),
                        String.format("%s %s，高于危急值上限 %s %s",
                                rule.label(), format(value), trim(rule.criticalHigh()), rule.unit())));
            }
        }
        return Optional.empty();
    }

    private static boolean matches(Rule rule, String code, String name) {
        if (StringUtils.hasText(code) && rule.code().equals(code)) {
            return true;
        }
        return StringUtils.hasText(name) && name.contains(rule.nameKeyword());
    }

    /**
     * 单位归一：大小写、空格、上标差异（10^9/L 与 10⁹/L、10*9/L）
     */
    private static String normalizeUnit(String unit) {
        if (!StringUtils.hasText(unit)) {
            return "";
        }
        return unit.trim().toLowerCase(Locale.ROOT)
                .replace(" ", "")
                .replace("⁹", "^9").replace("¹²", "^12").replace("⁶", "^6")
                .replace("*", "^");
    }

    private static String trim(Double value) {
        if (value == Math.floor(value)) {
            return String.valueOf(value.longValue());
        }
        return String.valueOf(value);
    }

    private static String format(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}

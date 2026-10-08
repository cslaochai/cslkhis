package com.his.system.enums;

import com.his.common.util.TextUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 质量单位枚举（码值口径 = 字典 his_dose_unit 的 g/mg/ug，另认中文与同义写法）。
 */
public enum MassUnitEnum {

    /**
     * 毫克（基准，系数 1）
     */
    MG(new BigDecimal("1"), "mg", "毫克"),

    /**
     * 克
     */
    G(new BigDecimal("1000"), "g", "克"),

    /**
     * 微克
     */
    UG(new BigDecimal("0.001"), "ug", "μg", "mcg", "微克");

    /**
     * 折成 mg 的乘数
     */
    private final BigDecimal factorToMg;

    /**
     * 全部合法写法。<b>第一个就是字典码值</b>（his_dose_unit：g/mg/ug），其后是同义写法。
     * 匹配时大小写不敏感（库里实测混着 MG / Mg）。
     */
    private final List<String> aliases;

    MassUnitEnum(BigDecimal factorToMg, String... aliases) {
        this.factorToMg = factorToMg;
        this.aliases = List.of(aliases);
    }

    /**
     * 折成 mg 的乘数（拿上限值 × 它即得 mg 口径的极量）
     */
    public BigDecimal getFactorToMg() {
        return factorToMg;
    }

    /**
     * 按写法反查枚举项；大小写与首尾空白不敏感。
     *
     * @return null 表示该写法不是质量单位（ml / IU / 片 / 空 / null）
     */
    public static MassUnitEnum fromUnit(String unit) {
        if (!TextUtil.hasText(unit)) {
            return null;
        }
        String key = unit.trim().toLowerCase();
        for (MassUnitEnum e : values()) {
            for (String alias : e.aliases) {
                if (alias.equals(key)) {
                    return e;
                }
            }
        }
        return null;
    }

    /**
     * 数值 + 单位 → mg
     *
     * @return null 表示不是质量单位，或数值为 null（两种情况上层都当作「这条不判」）
     */
    public static BigDecimal toMg(BigDecimal value, String unit) {
        if (value == null) {
            return null;
        }
        MassUnitEnum e = fromUnit(unit);
        return e == null ? null : value.multiply(e.factorToMg);
    }

    /**
     * 质量记法完整正则：{@code 数值 + 空白 + 质量单位}，
     * 单位后不能再跟字母（挡 {@code 10iu/ml} 这类混写）。
     * <p>
     * 单位片段按别名长度降序拼，保证长的先试。
     */
    public static Pattern massPattern() {
        List<String> all = new ArrayList<>();
        for (MassUnitEnum e : values()) {
            all.addAll(e.aliases);
        }
        all.sort(Comparator.comparingInt(String::length).reversed());
        return Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(" + String.join("|", all) + ")(?![A-Za-z])",
                Pattern.CASE_INSENSITIVE);
    }
}
package com.his.system.enums;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 质量单位枚举（码值口径 = 字典 {@code his_dose_unit} 的 g/mg/ug，另认中文与同义写法）。
 *
 * <p><b>为什么必须有这个枚举</b>：单位写法有 8种（mcg/μg/ug/mg/g/微克/毫克/克），改前这 8 个在两处各手写一遍——
 * {@code DosageTextParser.MASS} 正则与 {@code toMg} 的 switch。加一个新单位若只改一处：
 * 只改正则 → 抽得出值但换算返回 null；只改 switch → 正则根本匹配不到。
 * 两种都是<b>静默漏判</b>（返回 null 上层当作「这条不判」，不报错），所以单位清单必须只有一个出口。
 *
 * <p><b>为什么不放 IU</b>：胰岛素笔规格写着 300IU/支，但单据上的「1」是旋出来的刻度数而不是 300IU，
 * 放进来会批量造出假超量（见 {@code DoseLimitUpsertDTO} 同源说明）。
 *
 * <p><b>别名 vs 正则的坑</b>：{@link #massAlternation()} 必须按别名<b>长度降序</b>拼，否则
 * {@code 0.5mg} 会被 {@code g} 抢走前半段，解析出 0.5 克。长度降序是硬约束，不是排版偏好。
 */
@Getter
public enum MassUnitEnum {

    /**
     * 毫克（基准，系数 1）
     */
    MG("mg", new BigDecimal("1"), "mg", "毫克"),

    /**
     * 克
     */
    G("g", new BigDecimal("1000"), "g", "克"),

    /**
     * 微克
     */
    UG("ug", new BigDecimal("0.001"), "ug", "μg", "mcg", "微克");

    /**
     * 折成 mg 的乘数
     */
    private final BigDecimal factorToMg;

    /**
     * 字典码值（his_dose_unit：g/mg/ug）
     */
    private final String code;

    /**
     * 全部合法写法，含 code 本身。匹配时大小写不敏感（库里实测混着 MG / Mg）。
     */
    private final List<String> aliases;

    MassUnitEnum(String code, BigDecimal factorToMg, String... aliases) {
        this.code = code;
        this.factorToMg = factorToMg;
        this.aliases = List.of(aliases);
    }

    /**
     * 按写法反查枚举项；大小写与首尾空白不敏感。
     *
     * @return null 表示该写法不是质量单位（ml / IU / 片 / 空 / null）
     */
    public static MassUnitEnum fromUnit(String unit) {
        if (unit == null || unit.isBlank()) {
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
     * 写法是否合法（写入侧校验用；null 与非质量单位都不合法）
     */
    public static boolean isValidUnit(String unit) {
        return fromUnit(unit) != null;
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
     * 质量记法的正则片段（供 {@code DosageTextParser.MASS} 拼装）。
     * <p>
     * 按别名长度降序拼，保证长的先试——{@code mg} 必须先于 {@code g}，否则 0.5mg 被读成 0.5g。
     */
    public static String massAlternation() {
        List<String> all = new ArrayList<>();
        for (MassUnitEnum e : values()) {
            all.addAll(e.aliases);
        }
        all.sort(Comparator.comparingInt(String::length).reversed());
        return String.join("|", all);
    }

    /**
     * 质量记法完整正则：{@code 数值 + 质量单位}，单位后不能再跟字母（挡 {@code mg/ml} 这类混写）。
     */
    public static Pattern massPattern() {
        return Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(" + massAlternation() + ")(?![A-Za-z])",
                Pattern.CASE_INSENSITIVE);
    }
}
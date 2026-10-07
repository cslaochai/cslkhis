package com.his.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 急诊分诊级别（I~IV 级）—— 分诊级别列的<b>唯一权威码值</b>，数字越小越优先。
 *
 * <p>与门诊分诊等级 {@code TriageLevelEnum}（1-危重 2-急症 3-亚急 4-非急）是<b>两个不同含义</b>，
 * 码值重合但叫法不同，不许互相替换。
 *
 * <p>{@link #getText(Integer)} 的缺省文案「未定级」是刻意声明的例外：还没分诊是这一列的真实语义
 * （患者到达时先登记、分诊台随后定级），与 {@code SysGenderEnum} 的 null→「未知」同例；
 * 而<b>不在 1-4 内的脏值返回空串</b>，不冒充任何合法级别。
 */
@Getter
@AllArgsConstructor
public enum EmergencyTriageLevelEnum {

    /**
     * 1-I级濒危
     */
    CRITICAL(1, "I级濒危"),

    /**
     * 2-II级危重
     */
    SEVERE(2, "II级危重"),

    /**
     * 3-III级急症
     */
    URGENT(3, "III级急症"),

    /**
     * 4-IV级非急症
     */
    NON_URGENT(4, "IV级非急症");

    /**
     * 级别码
     */
    private final int code;

    /**
     * 级别文案
     */
    private final String label;

    /**
     * 码值 → 枚举实例；null 或不在 1-4 内返回 null（不兜底成某个合法档位）
     */
    public static EmergencyTriageLevelEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (EmergencyTriageLevelEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 码值→展示文案。null → 「未定级」（见类注释），脏值 → 空串 ""，绝不返回 null。
     */
    public static String getText(Integer code) {
        if (code == null) {
            return "未定级";
        }
        EmergencyTriageLevelEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用：null 返回「未知」，脏值返回「未知(n)」，保留原始码值便于排查。
     */
    public static String labelOrUnknown(Integer code) {
        EmergencyTriageLevelEnum item = fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }

    /**
     * 是否属于「红区」级别（I/II 级）—— 优先级判断的唯一出口，别处不要再各写一遍 {@code <= 2}。
     */
    public static boolean isRedZone(Integer code) {
        return code != null && (code == CRITICAL.code || code == SEVERE.code);
    }
}

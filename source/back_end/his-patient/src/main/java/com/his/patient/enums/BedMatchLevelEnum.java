package com.his.patient.enums;

import lombok.Getter;

/**
 * 床位匹配级别枚举（床位服务中心给候配患者推荐床位时算出的匹配度，1~4）。
 *
 * <p>两个维度交叉：同科室 / 跨科室 × 床位型完全匹配 / 可降级使用。
 * 分值（100/80/50/30）属于技术阈值不是码值，留在 {@code BedCenterServiceImpl} 侧。
 */
@Getter
public enum BedMatchLevelEnum {

    SAME_DEPT_SAME_TYPE(1, "本科室·同床型"),
    SAME_DEPT_DOWNGRADE(2, "本科室·可降级"),
    CROSS_DEPT_SAME_TYPE(3, "跨科·同床型"),
    CROSS_DEPT_DOWNGRADE(4, "跨科·可降级");

    private final int code;
    private final String label;

    BedMatchLevelEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static BedMatchLevelEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (BedMatchLevelEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /** 展示用码值 → 文案。null / 越界码值返回空串，不回落到某个合法级别。 */
    public static String getText(Integer code) {
        BedMatchLevelEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /** 异常 / 审计用：null 或越界码值返回「未知(n)」（null 本身渲染成「未知」）。 */
    public static String labelOrUnknown(Integer code) {
        BedMatchLevelEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

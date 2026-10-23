package com.his.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 急诊分诊意识状态码值（MEWS 五项之一）。
 *
 * <p>码值口径来自 WS/T 810—2022《基层医疗卫生机构急重患者判断及转诊技术标准》
 * 表 1「意识」维度：清楚 / 对声音有反应 / 对疼痛有反应 / 无反应。
 * 只放码值与文案，不做评分——评分规则在 {@code EmergencyMewsRules}，两边不许互相抄。
 */
@Getter
@AllArgsConstructor
public enum EmergencyConsciousnessEnum {

    /**
     * 1-清楚
     */
    CLEAR(1, "清楚"),

    /**
     * 2-对声音有反应
     */
    VOICE(2, "对声音有反应"),

    /**
     * 3-对疼痛有反应
     */
    PAIN(3, "对疼痛有反应"),

    /**
     * 4-无反应
     */
    UNRESPONSIVE(4, "无反应");

    /**
     * 意识码值
     */
    private final int code;

    /**
     * 意识文案
     */
    private final String label;

    public static EmergencyConsciousnessEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (EmergencyConsciousnessEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 展示口径：脏码值出空串；null 出「未采」（意识是 MEWS 必采项，缺项必须显式说出来，
     * 不能悄悄按 0 分算完还不告诉分诊台）
     */
    public static String getLabel(Integer code) {
        if (null == code) {
            return "未采";
        }
        EmergencyConsciousnessEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }
}

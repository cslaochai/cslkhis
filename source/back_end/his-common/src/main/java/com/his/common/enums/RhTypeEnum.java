package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * Rh 血型枚举
 */
@Getter
public enum RhTypeEnum {

    POSITIVE(1, "阳性"),
    NEGATIVE(2, "阴性");

    /**
     * 输血链 Rh 落库值：阳性
     */
    public static final String RH_POSITIVE = "阳";
    /**
     * 输血链 Rh 落库值：阴性
     */
    public static final String RH_NEGATIVE = "阴";
    private final int code;
    private final String label;

    RhTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * Rh 归一：{@code + / 阳性 / 阳} → 阳；{@code - / 阴性 / 阴} → 阴；
     * 其余（null / 脏值）原样返回，不猜。
     */
    public static String normalizeRh(String rh) {
        if (rh == null) {
            return null;
        }
        String s = rh.trim();
        if (RH_POSITIVE.equals(s) || "阳性".equals(s) || "+".equals(s)) {
            return RH_POSITIVE;
        }
        if (RH_NEGATIVE.equals(s) || "阴性".equals(s) || "-".equals(s)) {
            return RH_NEGATIVE;
        }
        return s;
    }

    /**
     * 输血链 Rh 取值是否合法（落库前校验：归一后必须是阳/阴）
     */
    public static boolean isValidRh(String rh) {
        String s = normalizeRh(rh);
        return RH_POSITIVE.equals(s) || RH_NEGATIVE.equals(s);
    }

    /**
     * 输血链 Rh 展示文案（列表展示用，如「A 型 Rh(+)」由调用方拼装）
     */
    public static String getRhText(String rh) {
        String s = normalizeRh(rh);
        if (RH_NEGATIVE.equals(s)) {
            return "Rh(-)";
        }
        return RH_POSITIVE.equals(s) ? "Rh(+)" : "";
    }

    public static RhTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (RhTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        RhTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        RhTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        RhTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

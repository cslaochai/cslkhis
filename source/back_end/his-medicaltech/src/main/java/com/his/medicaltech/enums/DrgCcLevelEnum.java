package com.his.medicaltech.enums;

import com.his.common.util.TextUtil;
import lombok.Getter;

/**
 * CC/MCC 级别（码值口径 = 并发症合并症目录的级别列：MCC-严重并发症合并症 CC-并发症合并症 NONE-无）。
 *
 * <p>声明顺序即严重程度（NONE &lt; CC &lt; MCC），取最严重的一档走 {@link #max}。
 */
@Getter
public enum DrgCcLevelEnum {

    NONE("NONE", "无"),
    CC("CC", "并发症合并症"),
    MCC("MCC", "严重并发症合并症");

    private final String code;
    private final String label;

    DrgCcLevelEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DrgCcLevelEnum fromCode(String code) {
        if (!TextUtil.hasText(code)) {
            return null;
        }
        for (DrgCcLevelEnum item : values()) {
            if (item.code.equalsIgnoreCase(code.trim())) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（NONE/CC/MCC；NONE 是目录里的「不分级」档，入组侧一律当无并发症处理）
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }

    /**
     * 展示用：脏值出空串
     */
    public static String getText(String code) {
        DrgCcLevelEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 排查用：保留原始码值
     */
    public static String labelOrUnknown(String code) {
        DrgCcLevelEnum item = fromCode(code);
        return item == null ? "未知(" + code + ")" : item.label;
    }

    /**
     * 取两者中更严重的一档（null 按 NONE）
     */
    public static DrgCcLevelEnum max(DrgCcLevelEnum a, DrgCcLevelEnum b) {
        if (a == null) {
            return b == null ? NONE : b;
        }
        if (b == null) {
            return a;
        }
        return a.ordinal() >= b.ordinal() ? a : b;
    }
}

package com.his.common.enums;

import lombok.Getter;

/**
 * 检验解读关注级别（LLM 输出 level 列口径：1-常规 2-关注 3-尽快处理）。
 */
@Getter
public enum LabAttentionLevelEnum {

    NORMAL(1, ""),
    ATTENTION(2, "【关注】"),
    URGENT(3, "【尽快处理】");

    private final int code;
    /**
     * 要点行展示前缀（常规级别无前缀）
     */
    private final String prefix;

    LabAttentionLevelEnum(int code, String prefix) {
        this.code = code;
        this.prefix = prefix;
    }

    public static LabAttentionLevelEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (LabAttentionLevelEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值 → 展示前缀。null / 未知码值返回空前缀。
     */
    public static String prefixOf(Integer code) {
        LabAttentionLevelEnum item = fromCode(code);
        return item == null ? "" : item.prefix;
    }
}

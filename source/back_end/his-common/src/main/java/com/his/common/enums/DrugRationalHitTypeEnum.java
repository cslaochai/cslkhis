package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 合理用药审查命中类型枚举（字符串码值，出参契约的命中类型列）
 */
@Getter
public enum DrugRationalHitTypeEnum {

    INTERACTION("INTERACTION", "相互作用"),
    DOSE_SINGLE("DOSE_SINGLE", "单次超量"),
    DOSE_DAILY("DOSE_DAILY", "日累计超量");

    private final String code;
    private final String label;

    DrugRationalHitTypeEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }

    public static DrugRationalHitTypeEnum fromCode(String code) {
        if (null == code) {
            return null;
        }
        for (DrugRationalHitTypeEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(String code) {
        DrugRationalHitTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(String code, String defaultValue) {
        DrugRationalHitTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(String code) {
        DrugRationalHitTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

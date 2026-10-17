package com.his.common.enums;

import lombok.Getter;

/**
 * 既往疾病当前控制情况枚举（已治愈/控制良好/未控制/随访中）。
 * <p>
 * 本列可空，空值表示「没填」；「随访中」是还没结论，不等于「未控制」。
 * <p>
 * 码值本身即落库文案（中文串），与字典不是同一口径，改这里就是改数据。
 */
@Getter
public enum PastDiseaseCurrentStatusEnum {

    CURED("已治愈"),
    WELL_CONTROLLED("控制良好"),
    UNCONTROLLED("未控制"),
    FOLLOWING_UP("随访中");

    private final String code;

    PastDiseaseCurrentStatusEnum(String code) {
        this.code = code;
    }

    public static PastDiseaseCurrentStatusEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (PastDiseaseCurrentStatusEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 与空串不合法）
     */
    public static boolean isValid(String code) {
        return code != null && !code.isEmpty() && fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在词表内（脏数据）一律返回空串，不回落到合法文案。
     */
    public static String getText(String code) {
        PastDiseaseCurrentStatusEnum item = fromCode(code);
        return item == null ? "" : item.code;
    }

    /**
     * 异常 / 审计用码值 → 文案。null 或词表外返回「未知(n)」，保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(String code) {
        PastDiseaseCurrentStatusEnum item = fromCode(code);
        if (item != null) {
            return item.code;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}

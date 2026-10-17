package com.his.common.enums;

import lombok.Getter;

/**
 * 用药史给药途径枚举（口服/注射/外用/吸入）。
 * <p>
 * 列注释带「等」，说明这一列的取值由操作员扩展；本枚举只收后端会拿来做判断的四档。
 * <p>
 * 码值本身即落库文案（中文串），与字典不是同一口径，改这里就是改数据。
 */
@Getter
public enum MedicationHistoryRouteEnum {

    ORAL("口服"),
    INJECTION("注射"),
    TOPICAL("外用"),
    INHALATION("吸入");

    private final String code;

    MedicationHistoryRouteEnum(String code) {
        this.code = code;
    }

    public static MedicationHistoryRouteEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (MedicationHistoryRouteEnum item : values()) {
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
        MedicationHistoryRouteEnum item = fromCode(code);
        return item == null ? "" : item.code;
    }

    /**
     * 异常 / 审计用码值 → 文案。null 或词表外返回「未知(n)」，保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(String code) {
        MedicationHistoryRouteEnum item = fromCode(code);
        if (item != null) {
            return item.code;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}

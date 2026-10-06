package com.his.ai.enums;

import lombok.Getter;

/**
 * 病情恶化预警级枚举（0-未触发 1-关注 2-高危，口径 = MEWS 总分阈值判定结果）。
 */
@Getter
public enum DeteriorationAlertLevelEnum {

    NONE(0, "未触发"),
    WATCH(1, "关注（MEWS ≥4，建议加测观察）"),
    CRITICAL(2, "高危（MEWS ≥6，建议立即评估）");

    private final int code;
    private final String label;

    DeteriorationAlertLevelEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DeteriorationAlertLevelEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DeteriorationAlertLevelEnum item : values()) {
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
     * 码值→展示文案。null 或不在枚举内（脏数据）返回空串 ""，绝不返回 null、不回落合法文案。
     */
    public static String getText(Integer code) {
        DeteriorationAlertLevelEnum item = fromCode(code);
        return item != null ? item.label : "";
    }

    /**
     * 码值→异常/审计文案。null 或不在枚举内返回「未知(n)」，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        if (code == null) {
            return "未知";
        }
        DeteriorationAlertLevelEnum item = fromCode(code);
        return item != null ? item.label : "未知(" + code + ")";
    }
}

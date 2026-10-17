package com.his.common.enums;

import lombok.Getter;

/**
 * 用药史用药状态枚举（进行中/已停用/已换药/已减量）。
 * <p>
 * 本列可空，写入口必须显式给值（新增默认进行中）：留 NULL 与某一档是两种语义，读取层不要互相回落。
 * <p>
 * 码值本身即落库文案（中文串），与字典不是同一口径，改这里就是改数据。
 */
@Getter
public enum MedicationHistoryStatusEnum {

    IN_PROGRESS("进行中"),
    STOPPED("已停用"),
    SWITCHED("已换药"),
    REDUCED("已减量");

    private final String code;

    MedicationHistoryStatusEnum(String code) {
        this.code = code;
    }

    public static MedicationHistoryStatusEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (MedicationHistoryStatusEnum item : values()) {
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
        MedicationHistoryStatusEnum item = fromCode(code);
        return item == null ? "" : item.code;
    }

    /**
     * 异常 / 审计用码值 → 文案。null 或词表外返回「未知(n)」，保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(String code) {
        MedicationHistoryStatusEnum item = fromCode(code);
        if (item != null) {
            return item.code;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}

package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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

    /**
     * 码值是否合法（写入侧校验用；null 与空串不合法）
     */
    public static boolean isValid(String code) {
        return code != null && !code.isEmpty() && fromCode(code) != null;
    }

    public static MedicationHistoryStatusEnum fromCode(String code) {
        if (null == code) {
            return null;
        }
        for (MedicationHistoryStatusEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(String code) {
        MedicationHistoryStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.code;
    }

    public static String getLabel(String code, String defaultValue) {
        MedicationHistoryStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.code;
    }

    public static String getLabelOrUnknown(String code) {
        MedicationHistoryStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.code;
    }
}

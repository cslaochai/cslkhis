package com.his.common.enums;

import lombok.Getter;

/**
 * 用药史药物类型枚举（处方药/非处方药/中药/保健品）。
 * <p>
 * 保健品与处方药的临床意义不同（相互作用问询范围不一样），不要为了少一档并成「其他」。
 * <p>
 * 码值本身即落库文案（中文串），与字典不是同一口径，改这里就是改数据。
 */
@Getter
public enum MedicationHistoryDrugTypeEnum {

    PRESCRIPTION("处方药"),
    OTC("非处方药"),
    TCM("中药"),
    SUPPLEMENT("保健品");

    private final String code;

    MedicationHistoryDrugTypeEnum(String code) {
        this.code = code;
    }

    public static MedicationHistoryDrugTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (MedicationHistoryDrugTypeEnum item : values()) {
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
        MedicationHistoryDrugTypeEnum item = fromCode(code);
        return item == null ? "" : item.code;
    }

    /**
     * 异常 / 审计用码值 → 文案。null 或词表外返回「未知(n)」，保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(String code) {
        MedicationHistoryDrugTypeEnum item = fromCode(code);
        if (item != null) {
            return item.code;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}

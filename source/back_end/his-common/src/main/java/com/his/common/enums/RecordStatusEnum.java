package com.his.common.enums;

import lombok.Getter;

/**
 * 病历状态枚举
 */
@Getter
public enum RecordStatusEnum {

    DRAFT(1, "草稿"),
    SUBMITTED(2, "已提交"),
    ARCHIVED(3, "已归档"),
    VOIDED(4, "已作废");

    private final int code;
    private final String label;

    RecordStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RecordStatusEnum fromCode(int code) {
        for (RecordStatusEnum status : values()) {
            if (status.code == code) return status;
        }
        return null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        RecordStatusEnum status = code == null ? null : fromCode(code);
        return status == null ? "" : status.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，保留原始码值便于排查。
     */
    public static String labelOrUnknown(Integer code) {
        RecordStatusEnum status = code == null ? null : fromCode(code);
        return status == null ? (code == null ? "未知" : "未知(" + code + ")") : status.label;
    }
}

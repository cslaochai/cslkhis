package com.his.emr.enums;

import lombok.Getter;

/**
 * 病历质控流转动作枚举
 */
@Getter
public enum RecordQcActionEnum {

    START(1, "发起送审"),
    APPROVE(2, "审核通过"),
    RETURN(3, "退回整改"),
    RESUBMIT(4, "整改提交"),
    FINAL(5, "终审通过");

    private final int code;
    private final String label;

    RecordQcActionEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RecordQcActionEnum fromCode(int code) {
        for (RecordQcActionEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        RecordQcActionEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，保留原始码值便于排查。
     */
    public static String labelOrUnknown(Integer code) {
        RecordQcActionEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

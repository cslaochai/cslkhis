package com.his.patient.enums;

import lombok.Getter;

/**
 * 病危重通知状态枚举
 */
@Getter
public enum NoticeStatusEnum {

    DRAFT(1, "草稿"),
    ISSUED(2, "已签发"),
    ACKED(3, "已签收"),
    VOIDED(4, "已作废");

    private final int code;
    private final String label;

    NoticeStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static NoticeStatusEnum fromCode(int code) {
        for (NoticeStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String getText(Integer code) {
        NoticeStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        NoticeStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

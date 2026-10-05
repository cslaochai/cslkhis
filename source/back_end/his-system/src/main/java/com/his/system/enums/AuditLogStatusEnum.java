package com.his.system.enums;

import lombok.Getter;

/**
 * 审计日志状态枚举（{@code sys_audit_log.status}：1-成功 0-失败）。
 *
 * <p><b>与 {@link OperStatusEnum}（操作日志 0-正常 1-异常）码值含义相反</b>，
 * 两张表各用各的枚举，不要图省事共用一个。
 *
 * <p>也不同于 {@link com.his.common.enums.AuditStatusEnum}（那是单据审核流转 0-3）。
 */
@Getter
public enum AuditLogStatusEnum {

    FAILURE(0, "失败"),
    SUCCESS(1, "成功");

    private final int code;
    private final String label;

    AuditLogStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AuditLogStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AuditLogStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /** 展示用码值 → 文案。null / 越界码值返回空串。 */
    public static String getText(Integer code) {
        AuditLogStatusEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /** 异常 / 审计用：null 或越界码值返回「未知(n)」（null 本身渲染成「未知」）。 */
    public static String labelOrUnknown(Integer code) {
        AuditLogStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

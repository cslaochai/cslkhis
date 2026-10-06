package com.his.report.enums;

import lombok.Getter;

/**
 * CDR 时间轴：检验记录状态文案（码值口径 = 库列注释）。
 */
@Getter
public enum CdrLabRecordStatusEnum {

    REGISTERED(1, "已登记"),
    SAMPLED(2, "已采样"),
    RECEIVED(3, "已接收"),
    TESTING(4, "检测中"),
    RESULTED(5, "已出结果"),
    REVIEWED(6, "已审核"),
    RELEASED(7, "已发布"),
    CANCELLED(8, "已取消");

    private final int code;
    private final String label;

    CdrLabRecordStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CdrLabRecordStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CdrLabRecordStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /** 展示用：null / 脏码值一律返回空串，绝不返回 null、绝不回落合法值 */
    public static String getText(Integer code) {
        CdrLabRecordStatusEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    /** 异常 / 审计用：null 或脏码值返回「未知(n)」，保留原始码值 */
    public static String labelOrUnknown(Integer code) {
        CdrLabRecordStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

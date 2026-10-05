package com.his.operation.enums;

import lombok.Getter;

/**
 * 麻醉随访状态枚举（码值口径 = biz_anesthesia_followup.followup_status 列注释）。
 */
@Getter
public enum AnesthesiaFollowupStatusEnum {

    DRAFT(0, "草稿"),
    DONE(1, "已完成");

    private final int code;
    private final String label;

    AnesthesiaFollowupStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AnesthesiaFollowupStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AnesthesiaFollowupStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    public static String labelOf(Integer code) {
        AnesthesiaFollowupStatusEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.getLabel();
    }

    /** Integer 码值判定：null 安全，语义同 == 比较 int 常量 */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }
}

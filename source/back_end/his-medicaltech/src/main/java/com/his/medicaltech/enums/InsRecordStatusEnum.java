package com.his.medicaltech.enums;

import lombok.Getter;

@Getter
public enum InsRecordStatusEnum {

    REGISTERED(1, "已登记"),
    SIGNED_IN(2, "已签到"),
    CHECKING(3, "检查中"),
    RESULTED(4, "已出结果"),
    REVIEWED(5, "已审核"),
    PUBLISHED(6, "已发布"),
    CANCELLED(7, "已取消");

    private final Integer code;
    private final String desc;

    InsRecordStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static InsRecordStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (InsRecordStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

    /** Integer 码值判定：null 安全，语义同 == 比较 int 常量 */
    public boolean is(Integer code) {
        return code != null && code.equals(this.code);
    }

    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null / 越界码值返回空串 ——
     * 「已登记」在医生站另有说法（已缴费待执行），那种措辞差异由调用侧按语境处理，不改这里的 label。
     */
    public static String getText(Integer code) {
        InsRecordStatusEnum e = getByCode(code);
        return e == null ? "" : e.desc;
    }

    /** 异常 / 审计用：null 或越界码值返回「未知(n)」（null 本身渲染成「未知」）。 */
    public static String labelOrUnknown(Integer code) {
        InsRecordStatusEnum e = getByCode(code);
        return e == null ? (code == null ? "未知" : "未知(" + code + ")") : e.desc;
    }
}
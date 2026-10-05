package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * 用血分级审批级别枚举（sql/93）。
 *
 * <p>口径：&lt;400ml 上级医师（主治及以上）；400~799ml 科主任；≥800ml 医务科。
 * 折算与判定见 {@code TransfusionRules}。
 */
@Getter
public enum TransfusionApproveLevelEnum {

    SENIOR_DOCTOR(1, "上级医师（主治及以上）"),
    DEPT_HEAD(2, "科主任"),
    MEDICAL_AFFAIR(3, "医务科");

    private final int code;
    private final String label;

    TransfusionApproveLevelEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static TransfusionApproveLevelEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TransfusionApproveLevelEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /** 展示用码值 → 文案。null 返回「—」；脏值返回空串。 */
    public static String getText(Integer code) {
        if (code == null) {
            return "—";
        }
        TransfusionApproveLevelEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」）。 */
    public static String labelOrUnknown(Integer code) {
        TransfusionApproveLevelEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    /** Integer 码值判定：null 安全，语义同 == 比较 int 常量 */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }
}

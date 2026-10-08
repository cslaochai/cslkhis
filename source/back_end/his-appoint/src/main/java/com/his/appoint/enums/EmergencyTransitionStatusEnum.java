package com.his.appoint.enums;

import lombok.Getter;

/**
 * 急诊状态可流转目标枚举（/emergency/updateStatus 的合法入参集合）。
 */
@Getter
public enum EmergencyTransitionStatusEnum {

    /**
     * 2-接诊：谁接诊谁负责，同时写诊断时间
     */
    TREATING(2, "接诊"),

    /**
     * 3-留观：必须先分配留观床位，否则住院分床会把同一张床再发出去
     */
    OBSERVATION(3, "留观"),

    /**
     * 5-离院：终态，先交还在观占床
     */
    DISCHARGED(5, "离院"),

    /**
     * 6-死亡：终态，先交还在观占床
     */
    DEAD(6, "死亡");

    private final int code;
    private final String label;

    EmergencyTransitionStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static EmergencyTransitionStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (EmergencyTransitionStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static String getText(Integer code) {
        EmergencyTransitionStatusEnum e = fromCode(code);
        return e == null ? "" : e.getLabel();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        EmergencyTransitionStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

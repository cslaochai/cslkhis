package com.his.appoint.enums;

import lombok.Getter;

/**
 * 急诊状态可流转目标枚举（{@code /emergency/updateStatus} 的合法入参集合）。
 *
 * <p>与 {@code com.his.common.enums.EmergencyStatusEnum}（急诊状态全量码值口径 0-6）的区别：
 * 后者回答「这条急诊记录现在处于哪个状态」，本枚举回答「这个接口允许把记录推成哪个状态」。
 * 两者不是同一个含义 —— 1候诊是登记后的初态、4转住院必须走 {@code /emergency/admit} 落真实入院登记，
 * 都不在本接口的可写集合里，所以不能直接拿全量枚举做入参校验（否则 0/1/4 会被放行）。
 *
 * <p>码值不连续（4 被跳过），故校验落 {@code @InEnum} 而非 {@code @Min/@Max}。
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

package com.his.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 急诊状态枚举 —— 急诊就诊状态的<b>唯一权威码值</b>（1-6）。
 *
 * <p>{@link #UNKNOWN} 是 {@link #fromCode} 的解析兜底，<b>不是可落库的业务码值</b>，
 * 因此 {@link #getText} 与 {@link #isValid} 都把它排除在外（脏值一律出空串、判为非法）。
 */
@Getter
@AllArgsConstructor
public enum EmergencyStatusEnum {

    /**
     * 1-候诊
     */
    WAITING(1, "候诊"),

    /**
     * 2-诊治中
     */
    TREATING(2, "诊治中"),

    /**
     * 3-留观
     */
    OBSERVATION(3, "留观"),

    /**
     * 4-转住院
     */
    ADMITTED(4, "转住院"),

    /**
     * 5-离院
     */
    DISCHARGED(5, "离院"),

    /**
     * 6-死亡
     */
    DEAD(6, "死亡"),

    /**
     * 未知状态（兜底处理，防止解析异常）
     */
    UNKNOWN(0, "未知状态");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 状态描述
     */
    private final String label;

    /**
     * 根据状态码获取对应的枚举实例
     *
     * @param code 状态码
     * @return 对应的枚举实例，若未匹配则返回 UNKNOWN
     */
    public static EmergencyStatusEnum fromCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (EmergencyStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return UNKNOWN;
    }

    /**
     * 码值是否合法（写入侧校验用；null 与解析兜底档 UNKNOWN 都不合法）
     */
    public static boolean isValid(Integer code) {
        EmergencyStatusEnum item = fromCode(code);
        return item != UNKNOWN;
    }

    /**
     * 码值→展示文案。null 或不在业务码值内（含脏值 0）返回空串 ""，绝不返回 null、不回落合法文案。
     */
    public static String getText(Integer code) {
        EmergencyStatusEnum item = fromCode(code);
        return item == UNKNOWN ? "" : item.label;
    }

    /**
     * 异常 / 审计用：null 或不在业务码值内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        EmergencyStatusEnum item = fromCode(code);
        if (item != UNKNOWN) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}

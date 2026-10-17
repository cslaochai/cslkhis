package com.his.common.enums;

import lombok.Getter;

/**
 * 日间手术登记单状态。
 */
@Getter
public enum DaySurgeryStatusEnum {

    WAIT_EVAL(1, "待评估"),
    EVAL_PASSED(2, "评估通过"),
    ARRANGED(3, "已安排"),
    OBSERVING(4, "术后观察"),
    DISCHARGED(5, "已出院"),
    CANCELED(6, "已取消"),
    TRANSFERRED(7, "已转住院");

    private final int code;
    private final String label;

    DaySurgeryStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DaySurgeryStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DaySurgeryStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * 展示用码值 → 文案。脏数据返回 null，由调用侧决定兜底，不回落到合法文案。
     */
    public static String getText(Integer code) {
        DaySurgeryStatusEnum item = fromCode(code);
        return item == null ? null : item.label;
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」，保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        DaySurgeryStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    /**
     * 终态（已出院 / 已取消 / 已转住院）：终态单据不再允许后续业务操作。
     */
    public static boolean isTerminal(Integer code) {
        DaySurgeryStatusEnum item = fromCode(code);
        return item == DISCHARGED || item == CANCELED || item == TRANSFERRED;
    }
}

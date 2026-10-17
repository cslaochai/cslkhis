package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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

    /**
     * 终态（已出院 / 已取消 / 已转住院）：终态单据不再允许后续业务操作。
     */
    public static boolean isTerminal(Integer code) {
        DaySurgeryStatusEnum item = fromCode(code);
        return item == DISCHARGED || item == CANCELED || item == TRANSFERRED;
    }

    public static DaySurgeryStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DaySurgeryStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DaySurgeryStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DaySurgeryStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DaySurgeryStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

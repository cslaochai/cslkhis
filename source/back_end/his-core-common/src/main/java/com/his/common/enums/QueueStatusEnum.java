package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 排队状态枚举 —— 候诊队列.queue_status 的唯一权威码值。
 */
@Getter
public enum QueueStatusEnum {

    /**
     * 已入队候诊中（签到入队后的初始态）
     */
    WAITING(2, "候诊中"),
    /**
     * 医生正在接诊
     */
    CONSULTING(3, "就诊中"),
    /**
     * 接诊结束
     */
    COMPLETED(4, "已就诊"),
    /**
     * 退号
     */
    CANCELLED(5, "已退号"),
    /**
     * 叫号未到
     */
    OVERDUE(6, "已过号"),
    /**
     * 跨日结转：签到入队后当天未接诊，队列作废（与「过号」区分，见类注释）
     */
    EXPIRED(7, "已失效");

    private final int code;
    private final String label;

    QueueStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 是否「还在队列里、可以被叫号」的状态
     */
    public static boolean isActive(Integer code) {
        return code != null && (code == WAITING.code || code == CONSULTING.code);
    }

    public static QueueStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (QueueStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        QueueStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        QueueStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        QueueStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

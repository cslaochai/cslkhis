package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 静配中心主单状态枚举（聚合派生：待审方 / 待排队 / 待调配 / 待核对 / 已完成 / 全拒配）。
 */
@Getter
public enum PivasBatchStatusEnum {

    PENDING_AUDIT(1, "待审方"),
    PENDING_QUEUE(2, "待排队"),
    PENDING_COMPOUND(3, "待调配"),
    PENDING_VERIFY(4, "待核对"),
    DONE(5, "已完成"),
    ALL_REJECTED(6, "全拒配");

    private final int code;
    private final String label;

    PivasBatchStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static PivasBatchStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (PivasBatchStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        PivasBatchStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        PivasBatchStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        PivasBatchStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

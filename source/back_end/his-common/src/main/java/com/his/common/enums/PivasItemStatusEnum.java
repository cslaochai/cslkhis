package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 静配中心调配明细状态枚举（医嘱 × 调配日：已拒配 / 待审方 / 已审方 / 已排队 / 已调配 / 已核对发放）。
 */
@Getter
public enum PivasItemStatusEnum {

    REJECTED(0, "已拒配"),
    PENDING_AUDIT(1, "待审方"),
    AUDITED(2, "已审方"),
    QUEUED(3, "已排队"),
    COMPOUNDED(4, "已调配"),
    VERIFIED(5, "已核对发放");

    private final int code;
    private final String label;

    PivasItemStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static PivasItemStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (PivasItemStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        PivasItemStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        PivasItemStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        PivasItemStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

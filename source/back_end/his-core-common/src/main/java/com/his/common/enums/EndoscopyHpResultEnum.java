package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 内镜幽门螺杆菌检测结果
 */
@Getter
public enum EndoscopyHpResultEnum {

    NOT_CHECKED(0, "未查"),
    NEGATIVE(1, "阴性"),
    POSITIVE(2, "阳性");

    private final Integer code;
    private final String label;

    EndoscopyHpResultEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static EndoscopyHpResultEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (EndoscopyHpResultEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        EndoscopyHpResultEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        EndoscopyHpResultEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        EndoscopyHpResultEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

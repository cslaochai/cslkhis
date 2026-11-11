package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 体检单项结果异常标志（0-正常 1-异常 2-待查）。
 *
 * <p>与检验 {@code LabAbnormalFlagEnum}（0/1偏高/2偏低/3异常）码值不同义，不可互相指代。</p>
 */
@Getter
public enum CheckupAbnormalFlagEnum {

    NORMAL(0, "正常"),
    ABNORMAL(1, "异常"),
    PENDING(2, "待查");

    private final int code;
    private final String label;

    CheckupAbnormalFlagEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CheckupAbnormalFlagEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (CheckupAbnormalFlagEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串
    public static String getLabel(Integer code) {
        CheckupAbnormalFlagEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }
}

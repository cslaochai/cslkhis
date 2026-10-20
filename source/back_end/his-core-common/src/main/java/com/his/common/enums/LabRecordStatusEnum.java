package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 记录状态枚举类
 */
public enum LabRecordStatusEnum {

    REGISTERED(1, "已登记"),
    SAMPLED(2, "已采样"),
    RECEIVED(3, "已接收"),
    TESTING(4, "检测中"),
    RESULTED(5, "已出结果"),
    REVIEWED(6, "已审核"),
    RELEASED(7, "已发布"),
    CANCELLED(8, "已取消");

    private final int code;
    private final String description;

    LabRecordStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static LabRecordStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (LabRecordStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        LabRecordStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.description;
    }

    public static String getLabel(Integer code, String defaultValue) {
        LabRecordStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.description;
    }

    public static String getLabelOrUnknown(Integer code) {
        LabRecordStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.description;
    }
}
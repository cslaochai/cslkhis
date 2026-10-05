package com.his.emr.enums;

import lombok.Getter;

/**
 * 临床路径入径状态枚举
 */
@Getter
public enum PathwayEnrollStatusEnum {

    ENROLLED(1, "在径"),
    FINISHED(2, "已完成"),
    ABORTED(3, "已退径");

    private final int code;
    private final String label;

    PathwayEnrollStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PathwayEnrollStatusEnum fromCode(int code) {
        for (PathwayEnrollStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        PathwayEnrollStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}

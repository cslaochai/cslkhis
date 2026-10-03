package com.his.emr.enums;

import lombok.Getter;

/**
 * 处方公示状态枚举
 */
@Getter
public enum PublicityStatusEnum {

    PUBLISHED(1, "已公示"),
    NOT_PUBLISHED(0, "未公示");

    private final int code;
    private final String label;

    PublicityStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PublicityStatusEnum fromCode(int code) {
        for (PublicityStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案。 */
    public static String labelOf(Integer code) {
        PublicityStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}

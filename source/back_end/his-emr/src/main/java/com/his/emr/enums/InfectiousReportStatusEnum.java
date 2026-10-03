package com.his.emr.enums;

import lombok.Getter;

/**
 * 传染病报卡状态枚举
 */
@Getter
public enum InfectiousReportStatusEnum {

    PENDING(1, "待审核"),
    AUDITED(2, "已审核待直报"),
    DIRECT(3, "已直报"),
    RETURNED(4, "已退报");

    private final int code;
    private final String label;

    InfectiousReportStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static InfectiousReportStatusEnum fromCode(int code) {
        for (InfectiousReportStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        InfectiousReportStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}

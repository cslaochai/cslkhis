package com.his.patient.enums;

import lombok.Getter;

/**
 * 护理质控指标事实来源枚举
 */
@Getter
public enum QcIndicatorSourceEnum {

    CHECK(1, "检查表"),
    FACT(2, "不良事件+住院事实");

    private final int code;
    private final String label;

    QcIndicatorSourceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static QcIndicatorSourceEnum fromCode(int code) {
        for (QcIndicatorSourceEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        QcIndicatorSourceEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}

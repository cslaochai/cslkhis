package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 质控触发来源枚举：这张质控单是被谁叫出来的。
 *
 * <p>与 qcType 正交：qcType 答「跑了哪几个维度的规则」，本枚举答「由谁发起」。
 * 分不开就无法回答「引擎判出的缺陷」与「质控员判出的缺陷」，而这两者的整改责任人不同。
 */
@Getter
public enum QcTriggerSourceEnum {

    MANUAL(1, "人工"),
    CLINIC_SUBMIT(2, "结诊自动"),
    AUTO_RESCAN(3, "定时补扫");

    private final int code;
    private final String label;

    QcTriggerSourceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static QcTriggerSourceEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (QcTriggerSourceEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        QcTriggerSourceEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        QcTriggerSourceEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        QcTriggerSourceEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 物联网设备绑定方式（biz_iot_device_bind.bind_source）
 * <p>
 * 真实医院里护士用 PDA 扫泵身条码完成绑定；没有扫码枪的环境由护士在页面上手工选设备号，两种都落同一张绑定表。
 */
@Getter
public enum IotBindSourceEnum {

    SCAN(1, "扫码"),
    MANUAL(2, "手工"),
    /** 设备科共享调配发放后绑定，区别于现场扫码/手工补录 */
    DISPATCH(3, "共享调配");

    private final int code;
    private final String label;

    IotBindSourceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static IotBindSourceEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (IotBindSourceEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        IotBindSourceEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        IotBindSourceEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        IotBindSourceEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 物联网设备绑定状态（biz_iot_device_bind.bind_status）
 * <p>
 * 「在用」= 设备当前正挂在这位在院患者身上，采样数据按这条绑定算患者的；
 * 「已解绑」= 换泵/治疗结束/出院时回收，行保留用于回溯。
 */
@Getter
public enum IotBindStatusEnum {

    IN_USE(1, "在用"),
    RELEASED(2, "已解绑");

    private final int code;
    private final String label;

    IotBindStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static IotBindStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (IotBindStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        IotBindStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        IotBindStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        IotBindStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

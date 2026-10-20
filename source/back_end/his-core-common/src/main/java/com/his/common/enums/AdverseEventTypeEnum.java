package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 医疗不良事件类型枚举
 */
@Getter
public enum AdverseEventTypeEnum {

    DRUG(1, "药品"),
    FALL(2, "跌倒坠床"),
    PRESSURE_INJURY(3, "压力性损伤"),
    OCCUPATIONAL_EXPOSURE(4, "职业暴露"),
    SURGERY(5, "手术相关"),
    INFUSION_TRANSFUSION(6, "输液输血"),
    TUBE(7, "管路"),
    INFECTION(8, "院感"),
    EQUIPMENT(9, "设备"),
    INFO_SECURITY(10, "信息安全"),
    OTHER(11, "其他");

    private final int code;
    private final String label;

    AdverseEventTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AdverseEventTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (AdverseEventTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        AdverseEventTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        AdverseEventTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        AdverseEventTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

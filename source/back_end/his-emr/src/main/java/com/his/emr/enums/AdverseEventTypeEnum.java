package com.his.emr.enums;

import lombok.Getter;

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

    public static AdverseEventTypeEnum fromCode(int code) {
        for (AdverseEventTypeEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        AdverseEventTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}

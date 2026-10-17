package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 感染监测观察对象（1-医生 2-护士 3-工勤/其他）。
 *
 * <p>与 {@code com.his.common.enums.StaffTypeEnum}（岗位类别：医生/护理/医技/药学…）不是同一口径，勿混用。
 */
@Getter
public enum InfectionObsObjectEnum {

    DOCTOR(1, "医生"),
    NURSE(2, "护士"),
    SUPPORT(3, "工勤/其他");

    private final int code;
    private final String label;

    InfectionObsObjectEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static InfectionObsObjectEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (InfectionObsObjectEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        InfectionObsObjectEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        InfectionObsObjectEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }
}

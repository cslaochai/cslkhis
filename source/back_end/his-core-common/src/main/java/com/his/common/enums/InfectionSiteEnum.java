package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 感染部位字典（感染监测用，字符串码值）。
 */
@Getter
public enum InfectionSiteEnum {

    LOWER_RESPIRATORY("1", "下呼吸道"),
    URINARY("2", "泌尿道"),
    GASTROINTESTINAL("3", "胃肠道"),
    SURGICAL_INCISION("4", "手术切口"),
    BLOODSTREAM("5", "血流感染"),
    SKIN_SOFT_TISSUE("6", "皮肤软组织"),
    INTRA_ABDOMINAL("7", "腹腔内"),
    OTHER("9", "其他");

    private final String code;
    private final String label;

    InfectionSiteEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static InfectionSiteEnum fromCode(String code) {
        if (null == code) {
            return null;
        }
        for (InfectionSiteEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(String code) {
        InfectionSiteEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(String code, String defaultValue) {
        InfectionSiteEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }
}

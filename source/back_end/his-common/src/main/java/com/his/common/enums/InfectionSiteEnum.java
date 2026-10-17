package com.his.common.enums;

import lombok.Getter;

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
        if (code == null) {
            return null;
        }
        for (InfectionSiteEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 展示用码值 → 文案。脏数据返回 null，由调用侧决定兜底，不回落到合法文案。
     */
    public static String getText(String code) {
        InfectionSiteEnum item = fromCode(code);
        return item == null ? null : item.label;
    }
}

package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 门诊公共工位类别枚举（sql/277）。
 *
 * <p>只覆盖全院公共工位，不含各专科科室自己的分诊台（那类挂科室，后续扩展）。</p>
 */
@Getter
public enum OpdStationKindEnum {

    /** 门诊总导诊台：大厅咨询/引路 */
    GUIDE(1, "总导诊"),
    /** 门诊分诊台：挂号后分诊到科室候诊 */
    TRIAGE(2, "门诊分诊台"),
    /** 门诊输液室：静脉输液在岗护理 */
    INFUSION(3, "输液室"),
    /** 门诊注射室：肌肉/皮下注射在岗护理 */
    INJECT(4, "注射室"),
    /** 门诊采血室：静脉采血在岗护理 */
    BLOOD(5, "采血室"),
    /** 其他公共工位 */
    OTHER(9, "其他");

    private final int code;
    private final String label;

    OpdStationKindEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static OpdStationKindEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (OpdStationKindEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        OpdStationKindEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        OpdStationKindEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        OpdStationKindEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

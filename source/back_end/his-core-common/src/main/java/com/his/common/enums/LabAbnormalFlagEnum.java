package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 检验结果异常标志枚举（0-正常 1-偏高 2-偏低 3-异常）。
 * <p>
 * 3-异常 用于定性结果不符（分不清高低）；「未判定」没有独立码值，读取层必须靠判定留痕区分，
 * 否则会把「不知道」显示成 0-正常。超声测量值列与本列同码同义，共用本枚举；
 * 患者端解读状态是另一套码值，不许指过来。
 */
@Getter
public enum LabAbnormalFlagEnum {

    NORMAL(0, "正常"),
    HIGH(1, "偏高"),
    LOW(2, "偏低"),
    ABNORMAL(3, "异常");

    private final int code;
    private final String label;

    LabAbnormalFlagEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * Integer 码值判定：null 安全，语义同 == 比较 int 常量
     */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }

    public static LabAbnormalFlagEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (LabAbnormalFlagEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        LabAbnormalFlagEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        LabAbnormalFlagEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        LabAbnormalFlagEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

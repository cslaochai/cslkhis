package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 麻醉随访轮次（1-术后即刻 2-术后24h 3-术后48h；其余轮次按「第N轮·追加随访」兜底）。
 */
@Getter
public enum AnesthesiaFollowupRoundEnum {

    R1(1, "第1轮·术后即刻"),
    R2(2, "第2轮·术后24h"),
    R3(3, "第3轮·术后48h");

    private final int code;
    private final String label;

    AnesthesiaFollowupRoundEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AnesthesiaFollowupRoundEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (AnesthesiaFollowupRoundEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 返回label值
     */
    public static String getLabel(Integer code, String defaultValue) {
        AnesthesiaFollowupRoundEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    /**
     * 返回label值。1~3 取 label；超出枚举的轮次是「追加随访」这一真实档位，按「第N轮·追加随访」渲染。
     */
    public static String getLabel(Integer code) {
        if (null == code) {
            return "—";
        }
        AnesthesiaFollowupRoundEnum item = fromCode(code);
        return item == null ? "第" + code + "轮·追加随访" : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        AnesthesiaFollowupRoundEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

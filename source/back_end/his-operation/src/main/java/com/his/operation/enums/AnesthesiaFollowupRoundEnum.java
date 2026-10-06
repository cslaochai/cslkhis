package com.his.operation.enums;

import lombok.Getter;

/**
 * 麻醉随访轮次（1-术后即刻 2-术后24h 3-术后48h；其余轮次按「第N轮·追加随访」兜底）。
 *
 * <p>码值→文案的唯一出口（原 {@code FollowupAdverseItems#roundText} 已上移至此）。
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
        if (code == null) {
            return null;
        }
        for (AnesthesiaFollowupRoundEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 码值→展示文案。1~3 取 label；超出枚举的轮次按「第N轮·追加随访」兜底（与原 roundText 一致）。
     */
    public static String getText(Integer roundNo) {
        if (roundNo == null) {
            return "—";
        }
        AnesthesiaFollowupRoundEnum e = fromCode(roundNo);
        return e != null ? e.label : "第" + roundNo + "轮·追加随访";
    }

    public static String labelOrUnknown(Integer roundNo) {
        if (roundNo == null) {
            return "未知";
        }
        AnesthesiaFollowupRoundEnum e = fromCode(roundNo);
        return e != null ? e.label : "未知(" + roundNo + ")";
    }
}

package com.his.common.enums;

import lombok.Getter;

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

    public static LabAbnormalFlagEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (LabAbnormalFlagEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        LabAbnormalFlagEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        LabAbnormalFlagEnum item = fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }

    /**
     * Integer 码值判定：null 安全，语义同 == 比较 int 常量
     */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }
}

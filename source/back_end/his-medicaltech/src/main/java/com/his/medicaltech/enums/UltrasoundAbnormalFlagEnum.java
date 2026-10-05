package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * 超声测量值异常标志枚举（码值口径 = biz_ultrasound_measure.abnormal_flag 列注释）。
 *
 * <p>该码值没有字典表，后端即唯一文案口径。服务端按参考范围判定后写入；
 * 「未判定」（范围为空/值不可解析）不落库，是展示层概念，由 Service 侧返回 null flag 表达。
 */
@Getter
public enum UltrasoundAbnormalFlagEnum {

    NORMAL(0, "正常"),
    HIGH(1, "偏高"),
    LOW(2, "偏低");

    private final int code;
    private final String label;

    UltrasoundAbnormalFlagEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static UltrasoundAbnormalFlagEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (UltrasoundAbnormalFlagEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    /** null 安全码值判定，语义同 == 比较 int 常量 */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }
}

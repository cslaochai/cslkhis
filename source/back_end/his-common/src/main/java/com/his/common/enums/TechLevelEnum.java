package com.his.common.enums;

import lombok.Getter;

/**
 * 手术/操作级别枚举（1~4 级，四级风险最高）。
 *
 * <p>三处共用同一个码表：手术申请单的手术级别、日间手术准入目录的手术级别、
 * 医疗技术授权台账的授权技术级别列（授权级别上限）。
 * 级别授权与手术级别必须是同一套码，否则「他有三级授权、这台是三级」这句话没法比。
 * <br>字典权威在本枚举，码值改动必须同步 {@code sql/49} 的 {@code his_operation_level} 段。
 */
@Getter
public enum TechLevelEnum {

    LEVEL_1(1, "一级"),
    LEVEL_2(2, "二级"),
    LEVEL_3(3, "三级"),
    LEVEL_4(4, "四级");

    private final int code;
    private final String label;

    TechLevelEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static TechLevelEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TechLevelEnum level : values()) {
            if (level.code == code) {
                return level;
            }
        }
        return null;
    }

    /**
     * 未知码值渲染成「未知(码值)」，绝不回落成某个合法级别
     */
    public static String getText(Integer code) {
        TechLevelEnum level = fromCode(code);
        return level == null ? "未知(" + code + ")" : level.getLabel();
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        TechLevelEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }
}

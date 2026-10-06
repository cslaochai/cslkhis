package com.his.patient.enums;

import lombok.Getter;

/**
 * 死亡证明领取联次枚举（码值口径 = biz_death_registration.received_copies 列注释）。
 *
 * <p>该列是<b>逗号分隔的复数字段</b>（如 "1,3"表示同时领取记录联与殡葬联），不是单值列，
 * 所以校验注解 {@code @InEnum} 表达不了（注解只能校验单值）——
 * 逐个值 split 后调 {@link #fromCode(String)} 判定，写在
 * {@code DeathRegistrationServiceImpl#normalizeCopies}。
 *
 * <p>code 用 String 而非 int：列里存的就是 "1" / "2" 这种字符串，拆出来的片段天然是字符串。
 */
@Getter
public enum DeathRegisterCopyEnum {

    RECORD(1, "记录联"),
    HOUSEHOLD(2, "户籍联"),
    FUNERAL(3, "殡葬联"),
    FAMILY(4, "家属联");

    private final int code;
    private final String label;

    DeathRegisterCopyEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /** 联次码值是否合法（复数字段逐值校验用；null / 空串不合法） */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }

    public static DeathRegisterCopyEnum fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        String trimmed = code.trim();
        for (DeathRegisterCopyEnum item : values()) {
            if (String.valueOf(item.code).equals(trimmed)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 展示用码值 → 文案。null / 空串 / 联次外一律返回空串，不回落到合法文案、也不暴露「未知(n)」——
     * 脏数据应由数据治理流程修复，而非界面伪装。
     */
    public static String getText(String code) {
        DeathRegisterCopyEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /** 异常 / 审计用：null / 空串 / 联次外返回「未知(n)」（null 本身渲染成「未知」），保留原始值便于排查。 */
    public static String labelOrUnknown(String code) {
        DeathRegisterCopyEnum item = fromCode(code);
        return item == null ? (code == null || code.isBlank() ? "未知" : "未知(" + code + ")") : item.label;
    }
}

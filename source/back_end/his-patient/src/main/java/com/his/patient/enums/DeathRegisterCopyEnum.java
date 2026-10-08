package com.his.patient.enums;

import com.his.common.util.TextUtil;
import lombok.Getter;

/**
 * 死亡证明领取联次枚举（码值口径 = biz_death_registration.received_copies 列注释）。
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
        if (!TextUtil.hasText(code)) {
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
        return item == null ? (!TextUtil.hasText(code) ? "未知" : "未知(" + code + ")") : item.label;
    }
}

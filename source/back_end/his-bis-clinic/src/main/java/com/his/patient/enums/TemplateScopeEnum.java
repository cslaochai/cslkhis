package com.his.patient.enums;

import lombok.Getter;

/**
 * 医嘱模板/组套共享范围枚举
 */
@Getter
public enum TemplateScopeEnum {

    PERSONAL(1, "个人"),
    DEPT(2, "科室"),
    HOSPITAL(3, "全院");

    private final int code;
    private final String label;

    TemplateScopeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static TemplateScopeEnum fromCode(int code) {
        for (TemplateScopeEnum item : values()) {
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
        return code != null && fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」——
     * 脏数据应由数据治理流程修复，而非界面伪装。
     */
    public static String getText(Integer code) {
        TemplateScopeEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }
    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，
     * 用于业务异常消息或审计日志，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        TemplateScopeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

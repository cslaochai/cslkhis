package com.his.system.enums;

import lombok.Getter;

/**
 * 登录日志状态枚举（{@code sys_login_log.login_status}：0-成功 1-失败）。
 */
@Getter
public enum LoginStatusEnum {

    SUCCESS(0, "成功"),
    FAILURE(1, "失败");

    private final int code;
    private final String label;

    LoginStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static LoginStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (LoginStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /** 展示用码值 → 文案。null / 越界码值返回空串。 */
    public static String getText(Integer code) {
        LoginStatusEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /** 异常 / 审计用：null 或越界码值返回「未知(n)」（null 本身渲染成「未知」）。 */
    public static String labelOrUnknown(Integer code) {
        LoginStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

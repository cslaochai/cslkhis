package com.his.common.enums;

import lombok.Getter;

/**
 * 角色数据权限范围枚举（1-全部数据 2-自定义数据 3-本部门数据 4-本部门及以下 5-仅本人数据）
 */
@Getter
public enum DataScopeEnum {

    ALL(1, "全部数据"),
    CUSTOM(2, "自定义数据"),
    DEPT(3, "本部门数据"),
    DEPT_AND_CHILD(4, "本部门及以下"),
    SELF(5, "仅本人数据");

    private final int code;
    private final String label;

    DataScopeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DataScopeEnum fromCode(int code) {
        for (DataScopeEnum scope : values()) {
            if (scope.code == code) return scope;
        }
        return null;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        DataScopeEnum scope = code == null ? null : fromCode(code);
        return scope == null ? "" : scope.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        DataScopeEnum scope = code == null ? null : fromCode(code);
        if (scope != null) {
            return scope.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}

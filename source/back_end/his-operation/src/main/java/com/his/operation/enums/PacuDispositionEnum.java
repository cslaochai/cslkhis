package com.his.operation.enums;

import lombok.Getter;

/**
 * PACU 出室去向
 */
@Getter
public enum PacuDispositionEnum {

    WARD(1, "回病房"),
    ICU(2, "转ICU"),
    CONTINUE_OBSERVATION(3, "继续留观");

    private final Integer code;
    private final String label;

    PacuDispositionEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PacuDispositionEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PacuDispositionEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        PacuDispositionEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        PacuDispositionEnum item = fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}

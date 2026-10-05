package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * 内镜幽门螺杆菌（HP）检测结果枚举（码值口径 = biz_endoscopy_record.hp_result 列注释）。
 *
 * <p>该码值没有字典表，后端即唯一文案口径：翻译统一走 {@link #textOf}，
 * 取不到渲染「未知(n)」，绝不回落成看似合法的值。
 */
@Getter
public enum EndoscopyHpResultEnum {

    UNTESTED(0, "未查"),
    NEGATIVE(1, "阴性"),
    POSITIVE(2, "阳性");

    private final int code;
    private final String label;

    EndoscopyHpResultEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static EndoscopyHpResultEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (EndoscopyHpResultEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    /** null → null；未命中 → 「未知(n)」（渲染口径与 DictCacheService.text 一致） */
    public static String textOf(Integer code) {
        if (code == null) {
            return null;
        }
        EndoscopyHpResultEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.getLabel();
    }

    /** 写入口校验：0~2 之外的码值非法 */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }
}

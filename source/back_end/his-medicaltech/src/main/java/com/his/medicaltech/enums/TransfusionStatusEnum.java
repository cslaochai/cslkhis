package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * 输血流程状态枚举（码值口径 = sql/39 列注释，前端筛选值必须逐一对齐）。
 *
 * <p>取代原 {@code TransfusionLabels.ST_*} int 常量；文案单点仍在 TransfusionLabels
 * （未知码值一律渲染「未知(码值)」，绝不回落成合法值）。
 */
@Getter
public enum TransfusionStatusEnum {

    PENDING_CROSSMATCH(0, "待配血"),
    CROSSMATCHED(1, "已配血"),
    ISSUED(2, "已发血"),
    INFUSING(3, "输注中"),
    FINISHED(4, "已完成"),
    CANCELLED(5, "已取消");

    private final int code;
    private final String label;

    TransfusionStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static TransfusionStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TransfusionStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    /** Integer 码值判定：null 安全，语义同 == 比较 int 常量 */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }
}

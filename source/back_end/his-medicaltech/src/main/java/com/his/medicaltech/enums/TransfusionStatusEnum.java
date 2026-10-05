package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * 输血流程状态枚举（码值口径 = sql/39 列注释，前端筛选值必须逐一对齐）。
 *
 * <p>取代原壳类里的 {@code ST_*} int 常量；文案单点在本枚举的
 * {@code getText}（未知码值返回空串，绝不回落成合法值）。
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

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /** 展示用码值 → 文案。null 返回「—」；脏值返回空串，不回落成「已完成」——那等于把没做完记成做完了。 */
    public static String getText(Integer code) {
        if (code == null) {
            return "—";
        }
        TransfusionStatusEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        TransfusionStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

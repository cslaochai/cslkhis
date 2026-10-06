package com.his.equipment.enums;

import lombok.Getter;

import java.util.List;

/**
 * 设备类别枚举（码值口径 = sys_equipment.category 列注释）。
 *
 * <p>注意：类别在前端也是下拉来源，本枚举的 {@link #options()} 是给下拉用的有序码值表，
 * 前端字典若已存在同名字典，仍以后端枚举为唯一口径（AGENTS.md §13）。
 */
@Getter
public enum EquipCategoryEnum {

    IMAGING(1, "大型影像设备"),
    LAB_ANALYSIS(2, "检验分析设备"),
    LIFE_SUPPORT(3, "生命支持设备"),
    OR(4, "手术室设备"),
    EMERGENCY(5, "抢救设备"),
    ROUTINE(6, "常规诊疗设备"),
    OTHER(7, "其他设备");

    private final int code;
    private final String label;

    EquipCategoryEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static EquipCategoryEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (EquipCategoryEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /** 展示用：null 或脏值返回空串 */
    public static String getText(Integer code) {
        EquipCategoryEnum e = fromCode(code);
        return e == null ? "" : e.getLabel();
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」 */
    public static String labelOrUnknown(Integer code) {
        EquipCategoryEnum e = fromCode(code);
        return e == null ? (code == null ? "未知" : "未知(" + code + ")") : e.label;
    }

    /** 下拉用：全部码值按 code 升序 */
    public static List<EquipCategoryEnum> options() {
        return List.of(values());
    }
}

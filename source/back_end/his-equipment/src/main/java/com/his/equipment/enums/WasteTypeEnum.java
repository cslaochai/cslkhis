package com.his.equipment.enums;

import lombok.Getter;

/**
 * 医疗废物类别枚举（码值口径 = biz_medical_waste.waste_type 列注释，按《医疗废物分类目录》五类）。
 *
 * <p>本码值无字典表，后端即唯一文案口径：VO 文案统一走 {@link #getText}。
 */
@Getter
public enum WasteTypeEnum {

    INFECTIOUS(1, "感染性废物"),
    INJURIOUS(2, "损伤性废物"),
    PATHOLOGICAL(3, "病理性废物"),
    PHARMACEUTICAL(4, "药物性废物"),
    CHEMICAL(5, "化学性废物");

    private final int code;
    private final String label;

    WasteTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static WasteTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (WasteTypeEnum e : values()) {
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

    public static String getText(Integer code) {
        WasteTypeEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.getLabel();
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        WasteTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

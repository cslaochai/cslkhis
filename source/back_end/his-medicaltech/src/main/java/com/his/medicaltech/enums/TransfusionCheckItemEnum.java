package com.his.medicaltech.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 输血前双人核对要点（输血安全核查单的可核对部分）。
 *
 * <p>码值→文案的唯一出口（原 {@code TransfusionCheckItems} 的 {@code LABELS} 映射已上移至此）。
 * 输血是唯一要求双人核对的护理操作，核对结果是码值集合，必核项（1~6）缺失直接拒绝输注。
 */
@Getter
public enum TransfusionCheckItemEnum {

    RECEIVER_ID(1, "受血者姓名、住院号与腕带信息一致", true),
    BLOOD_TYPE(2, "受血者与血袋血型（ABO + Rh）相符", true),
    BLOOD_BAG(3, "血袋号、血液品种与规格与发血单一致", true),
    CROSSMATCH(4, "交叉配血结果相合（主侧/次侧）", true),
    APPEARANCE(5, "血液外观无异常（无溶血、无凝块、无气泡、无变色）", true),
    VALIDITY(6, "血袋有效期与包装完好", true),
    CONSENT(7, "输血知情同意书已签署", false),
    VITALS(8, "输注前生命体征已测量并记录", false);

    private final int code;
    private final String label;
    private final boolean required;

    TransfusionCheckItemEnum(int code, String label, boolean required) {
        this.code = code;
        this.label = label;
        this.required = required;
    }

    public static TransfusionCheckItemEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TransfusionCheckItemEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 码值→展示文案。null 给「—」；非法码值返回空串，不伪装成某一项。
     */
    public static String getText(Integer code) {
        if (code == null) {
            return "—";
        }
        TransfusionCheckItemEnum e = fromCode(code);
        return e != null ? e.label : "";
    }

    /**
     * 码值→异常 / 审计文案：非法码值返回「未知(n)」，保留原值便于排查。
     */
    public static String labelOrUnknown(Integer code) {
        if (code == null) {
            return "未知";
        }
        TransfusionCheckItemEnum e = fromCode(code);
        return e != null ? e.label : "未知(" + code + ")";
    }

    /** 必核项码值（1~6 缺任意一项不允许开始输注） */
    public static List<Integer> requiredCodes() {
        List<Integer> list = new ArrayList<>();
        for (TransfusionCheckItemEnum e : values()) {
            if (e.required) {
                list.add(e.code);
            }
        }
        return list;
    }

    /** 全部核对项（码→文案），供前端渲染勾选框 */
    public static Map<Integer, String> all() {
        Map<Integer, String> m = new LinkedHashMap<>();
        for (TransfusionCheckItemEnum e : values()) {
            m.put(e.code, e.label);
        }
        return m;
    }
}

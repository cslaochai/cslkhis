package com.his.common.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * 输血前双人核对要点（输血安全核查单的可核对部分）。
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

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 必核项码值（1~6 缺任意一项不允许开始输注）
     */
    public static List<Integer> requiredCodes() {
        List<Integer> list = new ArrayList<>();
        for (TransfusionCheckItemEnum e : values()) {
            if (e.required) {
                list.add(e.code);
            }
        }
        return list;
    }

    /**
     * 全部核对项（码→文案），供前端渲染勾选框
     */
    public static Map<Integer, String> all() {
        Map<Integer, String> m = new LinkedHashMap<>();
        for (TransfusionCheckItemEnum e : values()) {
            m.put(e.code, e.label);
        }
        return m;
    }

    public static TransfusionCheckItemEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (TransfusionCheckItemEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        if (null == code) {
            return "—";
        }
        TransfusionCheckItemEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        TransfusionCheckItemEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        TransfusionCheckItemEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

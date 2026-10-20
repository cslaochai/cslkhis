package com.his.common.enums;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * 血液品种枚举（输血申请单 / 血袋的品种列，1~6）。
 */
@Getter
public enum BloodComponentEnum {

    RED_CELL(1, "红细胞悬液"),
    PLASMA(2, "血浆"),
    PLATELET(3, "血小板"),
    CRYOPRECIPITATE(4, "冷沉淀"),
    WHOLE_BLOOD(5, "全血"),
    OTHER(6, "其他");

    private final int code;
    private final String label;

    BloodComponentEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 下拉选项用（保持 sql 列注释的码值顺序）
     */
    public static Map<Integer, String> options() {
        Map<Integer, String> map = new LinkedHashMap<>();
        for (BloodComponentEnum item : values()) {
            map.put(item.code, item.label);
        }
        return map;
    }

    /**
     * 是否为「红细胞类」（含全血）：ABO 按红细胞规则判相容
     */
    public static boolean isRedCellGroup(Integer component) {
        return RED_CELL.is(component) || WHOLE_BLOOD.is(component);
    }

    /**
     * 是否为「血浆类」（含冷沉淀）：ABO 按血浆规则判相容（与红细胞方向相反）
     */
    public static boolean isPlasmaGroup(Integer component) {
        return PLASMA.is(component) || CRYOPRECIPITATE.is(component);
    }

    /**
     * Integer 码值判定：null 安全，语义同 == 比较 int 常量
     */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }

    public static BloodComponentEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (BloodComponentEnum item : values()) {
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
        BloodComponentEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        BloodComponentEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        BloodComponentEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

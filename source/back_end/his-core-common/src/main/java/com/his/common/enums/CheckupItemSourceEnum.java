package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 体检项目来源枚举（1-检验项目 2-检查项目 3-一般检查）。
 *
 * <p>指向三张项目目录：检验项目 {@code sys_laboratory_item}、检查项目 {@code sys_inspection_item}、
 * 一般检查（诊查类）{@code sys_treatment_item}。体检套餐明细只存来源 + 目录项目ID/编码，
 * 名称与价格以目录为准。</p>
 */
@Getter
public enum CheckupItemSourceEnum {

    LABORATORY(1, "检验"),
    INSPECTION(2, "检查"),
    GENERAL(3, "一般");

    private final int code;
    private final String label;

    CheckupItemSourceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static CheckupItemSourceEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (CheckupItemSourceEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        CheckupItemSourceEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        CheckupItemSourceEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        CheckupItemSourceEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

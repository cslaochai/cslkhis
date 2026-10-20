package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 危急值触发来源枚举（RULE-规则自动识别）。
 * <p>
 * 当前只有「检验结果命中规则」这一条写入路径，所以这一档恒为 RULE；留出枚举位是给人工登记等第二来源用的。
 * 词表外的值（早期验证脚本写入的脏数据）一律按脏数据处理，不要替它编一个合法档。
 */
@Getter
public enum CriticalValueSourceEnum {

    RULE("RULE", "规则自动识别");

    private final String code;
    private final String label;

    CriticalValueSourceEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 与空串不合法）
     */
    public static boolean isValid(String code) {
        return code != null && !code.isEmpty() && fromCode(code) != null;
    }

    public static CriticalValueSourceEnum fromCode(String code) {
        if (null == code) {
            return null;
        }
        for (CriticalValueSourceEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(String code) {
        CriticalValueSourceEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(String code, String defaultValue) {
        CriticalValueSourceEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(String code) {
        CriticalValueSourceEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

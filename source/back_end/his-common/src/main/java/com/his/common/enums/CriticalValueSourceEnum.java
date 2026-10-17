package com.his.common.enums;

import lombok.Getter;

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

    public static CriticalValueSourceEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (CriticalValueSourceEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 与空串不合法）
     */
    public static boolean isValid(String code) {
        return code != null && !code.isEmpty() && fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案。
     */
    public static String getText(String code) {
        CriticalValueSourceEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(String code) {
        CriticalValueSourceEnum item = fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}

package com.his.common.enums;

import lombok.Getter;

/**
 * 护理交班摘要来源枚举（1-模型 2-规则模板）。
 * <p>
 * 出参展示码：模型串过话才标 1，模型不可用时整段回落模板拼装的交班要素标 2。
 * 与「表达层来源」（字符串 rule/model）是两套码值，别互相套用；本档的判据是摘要文本谁写的。
 */
@Getter
public enum NursingHandoverSourceEnum {

    MODEL(1, "模型"),
    TEMPLATE(2, "规则模板");

    private final int code;
    private final String label;

    NursingHandoverSourceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static NursingHandoverSourceEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (NursingHandoverSourceEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案。
     */
    public static String getText(Integer code) {
        NursingHandoverSourceEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        NursingHandoverSourceEnum item = fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}

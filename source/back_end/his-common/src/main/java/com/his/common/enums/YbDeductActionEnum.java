package com.his.common.enums;

import lombok.Getter;

/**
 * 医保扣款处理留痕动作枚举（与扣款状态是两个不同列的码值：状态描述单据走到哪一步，
 * 动作描述这一次处理干了什么，两者码值同为 1-6 但含义不同，禁止互相代替）
 */
@Getter
public enum YbDeductActionEnum {

    DRAFT_CREATE(1, "新建草稿"),
    APPEAL_SUBMIT(2, "发起申诉"),
    APPEAL_RESULT(3, "录入申诉结果"),
    CONFIRM_DEDUCT(4, "确认扣款并追责"),
    PAYBACK_INPUT(5, "录入缴回"),
    CANCEL(6, "作废");

    private final int code;
    private final String label;

    YbDeductActionEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static YbDeductActionEnum fromCode(int code) {
        for (YbDeductActionEnum action : values()) {
            if (action.code == code) return action;
        }
        return null;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        YbDeductActionEnum action = code == null ? null : fromCode(code);
        return action == null ? "" : action.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        YbDeductActionEnum action = code == null ? null : fromCode(code);
        if (action != null) {
            return action.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}

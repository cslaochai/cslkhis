package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static YbDeductActionEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (YbDeductActionEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        YbDeductActionEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        YbDeductActionEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        YbDeductActionEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

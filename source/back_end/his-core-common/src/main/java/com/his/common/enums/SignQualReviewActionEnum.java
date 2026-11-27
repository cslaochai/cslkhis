package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 签名资格复核处理动作（入参，不落列）。
 */
@Getter
public enum SignQualReviewActionEnum {

    KEEP(1, "保留证书"),
    REVOKE(2, "吊销证书");

    private final int code;
    private final String label;

    SignQualReviewActionEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static SignQualReviewActionEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (SignQualReviewActionEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        SignQualReviewActionEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        SignQualReviewActionEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        SignQualReviewActionEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

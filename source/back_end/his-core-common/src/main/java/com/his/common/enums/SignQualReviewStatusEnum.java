package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 签名资格复核状态（biz_sign_qual_review.status）。
 */
@Getter
public enum SignQualReviewStatusEnum {

    PENDING(0, "待复核"),
    KEPT(1, "保留证书"),
    REVOKED(2, "已吊销");

    private final int code;
    private final String label;

    SignQualReviewStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static SignQualReviewStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (SignQualReviewStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        SignQualReviewStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        SignQualReviewStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        SignQualReviewStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

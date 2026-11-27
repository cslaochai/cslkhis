package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 签名证书吊销类型（sys_sign_cert.revoke_type）。
 *
 * <p>分权口径：业务吊销（离职/转岗/违规停签等）由医务科决定；
 * 应急吊销（UKey 丢失、私钥/PIN 泄露、介质损坏等安全事件）由信息科立即执行、事后备案。
 */
@Getter
public enum SignCertRevokeTypeEnum {

    BUSINESS(1, "业务吊销"),
    EMERGENCY(2, "应急吊销");

    private final int code;
    private final String label;

    SignCertRevokeTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static SignCertRevokeTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (SignCertRevokeTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /** 展示：合法→label；null/脏值→空串 */
    public static String getLabel(Integer code) {
        SignCertRevokeTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    /** 调用方自带缺省文案 */
    public static String getLabel(Integer code, String defaultValue) {
        SignCertRevokeTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    /** 异常/审计：null/脏值→未知(n) */
    public static String getLabelOrUnknown(Integer code) {
        SignCertRevokeTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

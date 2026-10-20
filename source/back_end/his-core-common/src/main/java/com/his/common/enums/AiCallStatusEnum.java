package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * AI 调用状态。区分「超时」与「失败」是因为排查方向完全不同：
 */
@Getter
public enum AiCallStatusEnum {

    /**
     * 调用成功
     */
    SUCCESS(1, "成功"),

    /**
     * 调用失败（网络错误、协议错误、解析失败等）
     */
    FAILED(2, "失败"),

    /**
     * 调用超时
     */
    TIMEOUT(3, "超时"),

    /**
     * 未发起调用即降级（总开关关闭 / 能力开关关闭 / 配置不完整）
     */
    DEGRADED(4, "降级"),

    /**
     * 熔断中，未发起调用
     */
    CIRCUIT_OPEN(5, "熔断");

    private final int code;

    private final String label;

    AiCallStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static AiCallStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (AiCallStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        AiCallStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        AiCallStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        AiCallStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

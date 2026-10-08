package com.his.ai.enums;

import lombok.Getter;

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

    public static AiCallStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AiCallStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null / 越界码值返回空串 ——
     * 审计列表里一条历史脏数据不该把整页渲染成「未知」，空串更安全。
     */
    public static String getText(Integer code) {
        AiCallStatusEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        AiCallStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}

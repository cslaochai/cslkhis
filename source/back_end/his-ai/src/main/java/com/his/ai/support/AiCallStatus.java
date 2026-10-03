package com.his.ai.support;

import lombok.Getter;

/**
 * AI 调用状态。区分「超时」与「失败」是因为排查方向完全不同：
 * 超时看网络与超时配置，失败看密钥与协议。
 */
@Getter
public enum AiCallStatus {

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

    AiCallStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 按状态码取中文标签，供审计列表展示。未知码返回「未知」而不是抛异常 ——
     * 审计数据可能包含历史版本写入的码值，列表页不该因为一条历史脏数据整页报错。
     */
    public static String labelOf(Integer code) {
        if (code == null) {
            return "未知";
        }
        for (AiCallStatus status : values()) {
            if (status.code == code) {
                return status.label;
            }
        }
        return "未知";
    }
}

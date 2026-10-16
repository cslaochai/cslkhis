package com.his.api.service;

/**
 * 出码 / 查单防刷节流闸端口（his-pay 实现）。
 *
 * <p>三道闸：间隔闸（{@link #intervalRetryAfter}）、配额闸（{@link #assertQuota}）、查单节流（{@link #allowsChannelQuery}）。
 * miniapp 只依赖本端口，不直接依赖 his-pay 的 {@code PayRateLimitSupport}。
 */
public interface PayRateLimitApi {

    /**
     * 间隔闸：窗口内首次放行返回 {@code null}；窗口内再次请求返回「还需等待的秒数」。
     */
    Long intervalRetryAfter(String key, int ttlSeconds);

    /**
     * 退回间隔闸窗口（只在「占了闸、但这次没真把码出来」时调用）。
     */
    void release(String key);

    /**
     * 配额闸：固定窗口内次数封顶，超限抛异常。
     */
    void assertQuota(String key, int limit, int windowSeconds, String actionLabel);

    /**
     * 查单节流：距上次真查单不足间隔返回 {@code false}（本轮只看本地状态）。
     */
    boolean allowsChannelQuery(String key, int ttlSeconds);
}

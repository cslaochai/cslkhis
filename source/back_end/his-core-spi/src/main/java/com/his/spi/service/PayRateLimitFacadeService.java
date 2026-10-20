package com.his.spi.service;

/**
 * 出码与查单防刷节流端口：间隔闸、人员配额闸、查单节流三道闸，阈值与缓存键由调用方决定。
 *
 * <p>故障策略是 fail-open：缓存不可用时放行，防重组件故障绝不阻断收款。
 */
public interface PayRateLimitFacadeService {

    /**
     * 间隔闸：窗口内首次放行返回 null，窗口内再次请求返回「还需等待的秒数」。
     * 用于收费台扫码收款挡住连点出码与脚本刷码。
     */
    Long intervalRetryAfter(String key, int ttlSeconds);

    /**
     * 退回间隔闸窗口。只用于「占了闸但这次没真把码出来」——不能让收银员为一场失败干等整个窗口。
     */
    void release(String key);

    /**
     * 配额闸：固定窗口内次数封顶，超限抛业务异常。
     * 用于按收银员封顶出码次数，挡住「换个账单继续批量刷」。
     */
    void assertQuota(String key, int limit, int windowSeconds, String actionLabel);

    /**
     * 查单节流：距上次真查单不足间隔返回 false（本轮只看本地状态）。
     * 用于收款结果轮询，到账主要靠渠道通知，查单只是兜底。
     */
    boolean allowsChannelQuery(String key, int ttlSeconds);
}

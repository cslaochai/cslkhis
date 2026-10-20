package com.his.system.service;

import java.time.Duration;

/**
 * 请求幂等防重服务：SET key NX EX，同一 (scene, key) 在 TTL 内只放行第一次。
 *
 * <p>解决的是「同一请求被重放」（客户端防重提交的<b>服务端</b>那道闸），
 * 不是跨进程互斥 —— 单实例、多实例语义都成立，不引入分布式锁。
 * 典型用法：写操作接口收到客户端 requestId 后 tryAcquire，被拦即抛「请勿重复提交」；
 * 业务失败时 release 放行（允许修正后重试），成功则靠 TTL 自然过期（重放被拦）。
 */
public interface IdempotentGuardService {

    /**
     * 尝试占用一次防重名额。
     *
     * @param scene 业务场景标识（如 pay/refund），参与 Redis key，避免跨场景互撞
     * @param key   请求维度的防重键（如 "账单ID:requestId"），同一请求重试必须传同一个值
     * @param ttl   防重窗口时长：覆盖一次完整业务（提交 → 处理 → 返回）即可，不宜过长
     * @return true = 本次放行；false = TTL 窗口内已占用，应拒绝重放
     */
    boolean tryAcquire(String scene, String key, Duration ttl);

    /**
     * 释放防重名额：仅业务<b>失败</b>时调用，让用户修正后可重试。
     * 成功路径不释放 —— key 带 TTL，到期自动消失。
     * Redis 不可用时释放静默失败（TTL 兜底），不抛错。
     */
    void release(String scene, String key);
}

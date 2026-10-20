package com.his.system.service.impl;

import com.his.system.service.IdempotentGuardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 幂等防重的 Redis 实现：SETNX + 过期时间一步完成，原子防并发重放。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotentGuardServiceImpl implements IdempotentGuardService {

    /**
     * 防重 key 前缀：与序列号、缓存等其它 Redis key 命名域隔离。
     */
    private static final String KEY_PREFIX = "his:idem:";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean tryAcquire(String scene, String key, Duration ttl) {
        try {
            Boolean ok = stringRedisTemplate.opsForValue()
                    .setIfAbsent(KEY_PREFIX + scene + ":" + key, "1", ttl);
            return Boolean.TRUE.equals(ok);
        } catch (Exception e) {
            log.warn("[幂等防重] Redis 不可用，降级放行 scene={} key={}：{}", scene, key, e.getMessage());
            return true;
        }
    }

    @Override
    public void release(String scene, String key) {
        try {
            stringRedisTemplate.delete(KEY_PREFIX + scene + ":" + key);
        } catch (Exception e) {
            // 释放失败无碍：key 带 TTL，到期自动消失，至多让同 requestId 在窗口内被误拦
            log.warn("[幂等防重] 释放失败（TTL 兜底） scene={} key={}：{}", scene, key, e.getMessage());
        }
    }
}

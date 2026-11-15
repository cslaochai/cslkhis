package com.his.webfault.support;

import com.his.webfault.config.FaultLabProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 缓存击穿：热点 key（医生排班 / 号源余量）过期的那一瞬间，N 个请求同时回源，
 * 数据库或下游在 1 秒内收到 N 倍流量。与「缓存穿透」（查不存在的数据）不是一回事。
 * <p>
 * 同一个场景给两种实现：不加锁 vs 单飞（computeIfAbsent），用来在面试里对比
 * 「加个分布式锁/本地单飞」前后的下游调用数。
 */
@Component
public class CacheStampedeSimulator {

    private static final String KEY = "schedule:doctor:1001";
    private static final long DOWNSTREAM_MS = 800;

    private final FaultLabProperties props;
    private final FaultExecutor executor;
    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private final AtomicInteger downstreamCalls = new AtomicInteger();

    public CacheStampedeSimulator(FaultLabProperties props, FaultExecutor executor) {
        this.props = props;
        this.executor = executor;
    }

    public record CacheResult(String mode, int downstreamCalls, long elapsedMs, String value) {
    }

    /**
     * @param singleFlight true = 只有一个线程回源（修复后）；false = 每个线程各自回源（故障态）
     */
    public CacheResult run(int concurrency, boolean singleFlight) {
        int count = Math.max(1, Math.min(props.getMaxConcurrency(), concurrency));
        cache.remove(KEY);
        downstreamCalls.set(0);
        CountDownLatch ready = new CountDownLatch(count);
        CountDownLatch go = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(count);
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            executor.run("fault-cache-" + i, () -> {
                ready.countDown();
                try {
                    go.await();
                    if (singleFlight) {
                        cache.computeIfAbsent(KEY, this::loadFromDownstream);
                    } else {
                        String value = cache.get(KEY);
                        if (value == null) {
                            value = loadFromDownstream(KEY);
                            cache.put(KEY, value);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("[fault-lab] cache task failed: " + e.getClass().getSimpleName());
                } finally {
                    done.countDown();
                }
            });
        }
        try {
            ready.await(5, TimeUnit.SECONDS);
            go.countDown();
            done.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return new CacheResult(singleFlight ? "SINGLE-FLIGHT" : "STAMPEDE",
                downstreamCalls.get(), System.currentTimeMillis() - start, cache.get(KEY));
    }

    private String loadFromDownstream(String key) {
        downstreamCalls.incrementAndGet();
        try {
            Thread.sleep(DOWNSTREAM_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "08:00-12:00 余号 3";
    }
}

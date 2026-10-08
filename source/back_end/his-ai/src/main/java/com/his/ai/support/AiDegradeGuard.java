package com.his.ai.support;

import com.his.ai.config.AiConfigProvider;
import com.his.ai.config.AiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * 降级熔断守卫（按能力独立计数）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiDegradeGuard {

    private final AiConfigProvider aiConfigProvider;

    private final Map<String, CircuitState> states = new ConcurrentHashMap<>();

    /**
     * 累计降级次数（含未熔断时的单次降级）
     */
    private final Map<String, LongAdder> degradedCounts = new ConcurrentHashMap<>();

    /**
     * 最近一次失败原因，仅在成功调用后清除
     */
    private final Map<String, String> lastFailureReasons = new ConcurrentHashMap<>();

    /**
     * 该能力当前是否可调用（未熔断）
     */
    public boolean isAvailable(String capabilityKey) {
        CircuitState state = states.get(capabilityKey);
        if (state == null) {
            return true;
        }
        long openUntil = state.openUntil;
        if (openUntil == 0L) {
            return true;
        }
        if (openUntil > System.currentTimeMillis()) {
            return false;
        }
        // 冷却已结束，进入半开状态
        synchronized (state) {
            if (state.openUntil != 0L && state.openUntil <= System.currentTimeMillis()) {
                state.openUntil = 0L;
                state.failures.set(0);
                log.info("[AI] {} 熔断冷却结束，放行一次试探调用", capabilityKey);
            }
        }
        return true;
    }

    /**
     * 该能力当前是否处于熔断期
     */
    public boolean isOpen(String capabilityKey) {
        CircuitState state = states.get(capabilityKey);
        return state != null && state.openUntil > System.currentTimeMillis();
    }

    /**
     * 记一次降级。由 {@code AiExecutionService} 在返回空结果前调用。
     */
    public void recordDegrade(String capabilityKey) {
        degradedCounts.computeIfAbsent(capabilityKey, key -> new LongAdder()).increment();
    }

    /**
     * 记下本次失败的具体原因，供前端展示。
     * <p>
     * 「模型调用未成功」这句话对医生没有意义，「模型服务不可达：Connection refused」才有意义 ——
     * 前者只能让人干等重试，后者能直接指向运维动作。所以失败原因要一路带到最外层。
     */
    public void recordFailureReason(String capabilityKey, String reason) {
        lastFailureReasons.put(capabilityKey, reason == null ? "" : reason);
    }

    /**
     * 取最近一次失败原因，无记录时返回空串
     */
    public String lastFailureReason(String capabilityKey) {
        return lastFailureReasons.getOrDefault(capabilityKey, "");
    }

    /**
     * 累计降级次数
     */
    public long degradedCountOf(String capabilityKey) {
        LongAdder adder = degradedCounts.get(capabilityKey);
        return adder == null ? 0L : adder.sum();
    }

    public void recordSuccess(String capabilityKey) {
        CircuitState state = states.get(capabilityKey);
        if (state != null) {
            state.failures.set(0);
            state.openUntil = 0L;
        }
        // 成功后清掉历史失败原因：否则「最近失败原因」会一直挂在健康检查上误导排查
        lastFailureReasons.remove(capabilityKey);
    }

    public void recordFailure(String capabilityKey) {
        AiProperties properties = aiConfigProvider.get();
        CircuitState state = states.computeIfAbsent(capabilityKey, key -> new CircuitState());
        int failures = state.failures.incrementAndGet();
        int threshold = Math.max(1, properties.getCircuitFailureThreshold());
        if (failures >= threshold) {
            int cooldownSeconds = Math.max(1, properties.getCircuitBreakerSeconds());
            state.openUntil = System.currentTimeMillis() + cooldownSeconds * 1000L;
            log.warn("[AI] {} 连续失败 {} 次，触发熔断，{} 秒内直接降级",
                    capabilityKey, failures, cooldownSeconds);
        }
    }

    private static final class CircuitState {

        private final AtomicInteger failures = new AtomicInteger(0);

        /**
         * 熔断截止时间戳，0 表示未熔断
         */
        private volatile long openUntil = 0L;
    }
}

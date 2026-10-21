package com.his.charge.service.impl;

import com.his.charge.service.PayRateLimitService;
import com.his.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 出码 / 查单的防刷节流闸。
 *
 * <p>渠道侧不对商户的请求量做保护，主动查单是真出网；收款台连点、患者端页面多开轮询都会把请求成倍放大。
 * 本端用三道闸自限，每道只回答一个问题：
 * <ul>
 *   <li>{@link #intervalRetryAfter} 间隔闸：同一张业务单两次「必须重出码」之间的最小间隔，
 *       防连点 / 脚本把同一笔单刷成一堆支付单（聚合码虽在本地拼出，但每出一次都要建单、写缓存、占号段）；</li>
 *   <li>{@link #assertQuota} 配额闸：单个操作人（收银员 / 患者）在固定窗口内的总次数封顶，
 *       间隔闸管不到「换着业务单批量刷」；</li>
 *   <li>{@link #allowsChannelQuery} 查单节流：同一张支付单两次真打渠道查单之间的最小间隔，
 *       轮询节奏由前端决定、页面多开时成倍放大，但渠道状态不会秒级变。</li>
 * </ul>
 *
 * <p><b>故障策略统一 fail-open（Redis 不可用就放行业务动作）</b>：这三道都是减压闸、不是资金闸，
 * 把「Redis 一抖」变成「收银台出不了码 / 患者查不到付款结果」才是更大的事故。
 * 查单节流放行的方向也是「照常查单」——查单是收款事实的兜底来源，宁可多打渠道也不能不查。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayRateLimitServiceImpl implements PayRateLimitService {

    /** 节流计数走 Redis（StringRedisTemplate）；故障 fail-open 放行，见类注释 */
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 间隔闸：窗口内首次放行返回 {@code null}；窗口内再次请求返回「还需等待的秒数」。
     *
     * <p>TTL 只在第一次写入时定下、被拦时绝不续期 —— 每次命中都续期的话，
     * 连点脚本永远能把自己的窗口往前推，闸等于没设。
     *
     * @param key        业务单维度的闸 key（调用方按业务单 ID 拼）
     * @param ttlSeconds 间隔窗口（秒）
     * @return {@code null}=放行；否则为还需等待的秒数，调用方据此决定「复用原码」还是「拒绝并提示」
     */
    public Long intervalRetryAfter(String key, int ttlSeconds) {
        try {
            Boolean first = stringRedisTemplate.opsForValue()
                    .setIfAbsent(key, "1", ttlSeconds, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(first)) {
                return null;
            }
            Long ttl = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
            // key 理论上一定还在（刚刚 setIfAbsent 失败说明在）；ttl 异常时按完整窗口提示，不猜 0
            return ttl == null || ttl < 0 ? (long) ttlSeconds : ttl;
        } catch (Exception e) {
            log.warn("[支付节流] 间隔闸不可用，本次放行 key={} err={}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 退回间隔闸的窗口：只在「占了闸、但这次没真把码出来」时调用。
     *
     * <p>间隔闸的前提是「这张业务单刚出了一张还活着的码，别在患者扫完之前换掉它」；
     * 建单 / 出码 / 画图任一步失败时这个前提不成立，留着窗口只会让操作人对着「过于频繁」干等，
     * 把渠道故障说成操作人的错。退窗口不等于没闸 —— 按操作人封顶的配额闸照旧计数，恶意量还是被压住。
     *
     * <p>删除失败只留 warn：最坏结果就是这一次要等满窗口，不影响资金，也不该盖掉真正的失败原因。
     */
    public void release(String key) {
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("[支付节流] 间隔闸窗口退回失败 key={} err={}", key, e.getMessage());
        }
    }

    /**
     * 配额闸：固定窗口、自第一次请求起算，不滑动 —— 只为封顶恶意量，不做精细计费，
     * 滑动窗口每次都要重排的代价不值得。
     *
     * @param key            操作人维度的闸 key（调用方按收银员 / 患者 ID 拼）
     * @param limit          窗口内允许的次数上限
     * @param windowSeconds  固定窗口长度（秒）
     * @param actionLabel    动作名（如「出码」「下单」），只用于超限提示文案
     */
    public void assertQuota(String key, int limit, int windowSeconds, String actionLabel) {
        long issued;
        try {
            // 第一次：建一个初值 0 的窗口键并立刻计为第 1 次；之后每次自增
            Boolean first = stringRedisTemplate.opsForValue()
                    .setIfAbsent(key, "0", windowSeconds, TimeUnit.SECONDS);
            Long count = Boolean.TRUE.equals(first) ? 1L : stringRedisTemplate.opsForValue().increment(key);
            issued = count == null ? 1L : count;
        } catch (Exception e) {
            log.warn("[支付节流] 配额闸不可用，本次放行 key={} err={}", key, e.getMessage());
            return;
        }
        if (issued > limit) {
            throw new BusinessException(actionLabel + "次数异常（本窗口已达 " + issued + " 次），请稍后再试或联系信息科");
        }
    }

    /**
     * 查单节流：距上次真查单不足间隔返回 {@code false}（本轮只看本地状态，不打渠道）。
     *
     * <p>到账主要靠渠道异步通知，查单只是公网穿透断了时的兜底，所以只需覆盖「通知没来」这一种情况。
     * 与出码闸相反，这里故障时<b>照常查单</b>（返回 true）：查单是收款事实的兜底来源，不能被减压组件挡住。
     *
     * @param key        支付单维度的闸 key（调用方按支付单号拼）
     * @param ttlSeconds 两次查单的最小间隔（秒）
     * @return {@code true}=本次允许真查单；{@code false}=窗口内重复请求，只看本地状态
     */
    public boolean allowsChannelQuery(String key, int ttlSeconds) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.opsForValue()
                    .setIfAbsent(key, "1", ttlSeconds, TimeUnit.SECONDS));
        } catch (Exception e) {
            log.warn("[支付节流] 查单节流不可用，本次照常查单 key={} err={}", key, e.getMessage());
            return true;
        }
    }
}

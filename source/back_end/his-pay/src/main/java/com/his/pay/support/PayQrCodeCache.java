package com.his.pay.support;

import com.his.common.util.TextUtil;
import com.his.pay.service.PayOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 支付单已出码串的缓存（<b>按支付单号</b>，不按渠道分 key）。
 *
 * <p>为什么单独一个组件而不是塞进渠道实现：收款台切渠道时会出现「一张账单挂多张渠道码」，
 * 出码侧要按单号统一取回码串，若缓存散在各渠道实现里，调用方就得按渠道分支去问——
 * 那种分支一旦漏了一个渠道，表现是「同一个渠道切回来还多打一趟渠道」，且不会有任何报错。
 *
 * <p>码串在有效期内对同一商户订单号恒定不变（渠道按商户订单号幂等），所以出过一次就该记住：
 * 收银员点刷新、前端重连、弹层重开、渠道来回切，都只该花本地画图的钱。
 *
 * <p><b>故障策略与节流闸相反，这里是 fail-soft</b>：Redis 读不到就返回 null 退回真出码
 * （代价是多一趟渠道请求），写失败只留 warn（最坏下次重出）。缓存不是收款事实的来源，
 * 让它把出码顶死才是更大的事故。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayQrCodeCache {

    /**
     * key 前缀 + 支付单号。按单号做 key 而不是按业务单：一张账单可能同时挂着微信/支付宝两张码，
     * 业务单维度的 key 只能存其中一个，另一张就永远取不回来。
     */
    private static final String QR_KEY_PREFIX = "his:pay:qr:";

    /**
     * 码串存活时长（分钟）：比支付单有效期多留 1 分钟。
     * 缓存先于支付单过期，等于让一次正常刷新白打渠道一趟；缓存晚于支付单过期，取回的码对应的单已作废。
     */
    private static final int QR_TTL_MINUTES = PayOrderService.PAY_ORDER_EXPIRE_MINUTES + 1;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 记住这张单的码串。空白码串不写：占位空值会让下次取回得到「有缓存但没码」的假命中。
     */
    public void put(String payNo, String qrCode) {
        if (!TextUtil.hasText(payNo) || !TextUtil.hasText(qrCode)) {
            return;
        }
        try {
            stringRedisTemplate.opsForValue()
                    .set(QR_KEY_PREFIX + payNo, qrCode, QR_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("[出码缓存] 写入失败 payNo={} err={}", payNo, e.getMessage());
        }
    }

    /**
     * 取回这张单已出过的码串；没出过、已过期、或 Redis 不可用都返回 null（调用方退回真出码）。
     */
    public String get(String payNo) {
        if (!TextUtil.hasText(payNo)) {
            return null;
        }
        try {
            return stringRedisTemplate.opsForValue().get(QR_KEY_PREFIX + payNo);
        } catch (Exception e) {
            log.warn("[出码缓存] 读取失败，退回真出码 payNo={} err={}", payNo, e.getMessage());
            return null;
        }
    }

    /**
     * 丢弃码串：支付单被关闭/过期时调用。留着它会让「已作废的单」再被读出一张活着时的码，
     * 界面重画出一张渠道侧早已关掉的码，患者扫了只会得到一句「交易已关闭」。
     */
    public void evict(String payNo) {
        if (!TextUtil.hasText(payNo)) {
            return;
        }
        try {
            stringRedisTemplate.delete(QR_KEY_PREFIX + payNo);
        } catch (Exception e) {
            log.warn("[出码缓存] 清除失败 payNo={} err={}", payNo, e.getMessage());
        }
    }
}

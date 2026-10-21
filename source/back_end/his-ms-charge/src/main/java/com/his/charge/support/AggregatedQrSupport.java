package com.his.charge.support;

import com.his.common.enums.PayChannelEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.charge.config.PayAggregatedProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * 聚合支付码支撑（医院自有聚合网关的本地侧）。
 *
 * <p>职责只两件：① 把支付单号拼成患者可扫的聚合收款链接；② 模拟模式下的「已收款」登记与查单，
 * 让整条「出码 → 轮询 → 入账」在没有真实网关时也能跑通并验收。真实网关模式下这两项都不生效，
 * 收款事实只来自网关异步通知（见 {@code QrPayServiceImpl#confirmByAlipayNotify} 等）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AggregatedQrSupport {

    /**
     * 模拟收款标记（Redis key 前缀）：聚合码不绑定渠道，一个 payNo 只有一笔，按单号登记即可。
     */
    private static final String MOCK_PAID_KEY_PREFIX = "his:pay:mock:agg:paid:";

    /**
     * 模拟收款标记存活时长（小时），与一张码在窗口前的合理停留时间同量级。
     */
    private static final int MOCK_PAID_TTL_HOURS = 2;

    private final PayAggregatedProperties aggregatedProperties;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 把支付单号拼成患者可扫的聚合收款链接：医院自有网关地址 + 支付单号。
     * 患者用任意 App 扫都打开同一个收款页，由网关识别运行环境后拉起对应渠道付款。
     */
    public String buildQrContent(String payNo) {
        String base = aggregatedProperties.getQrBaseUrl();
        if (!TextUtil.hasText(base)) {
            base = "https://pay.hospital.com/qr/";
        }
        return base + payNo;
    }

    public boolean isMockEnabled() {
        return aggregatedProperties.isMockEnabled();
    }

    /**
     * 模拟收款登记：把「患者已照单付款」这件事记进 Redis，随后轮询查单就按实收入账。
     * channel 决定这笔模拟收款最终落在哪个资金渠道（影响流水与对账口径），不传默认微信。
     */
    public void markMockPaid(String payNo, Integer channel, BigDecimal amount) {
        if (!isMockEnabled()) {
            throw new BusinessException(
                    "聚合收款处于真实模式（pay.aggregated.mock-enabled=false），拒绝模拟收款：" + payNo);
        }
        Integer ch = channel != null ? channel : PayChannelEnum.WECHAT.getCode();
        if (PayChannelEnum.fromCode(ch) == null) {
            throw new BusinessException("模拟收款渠道不合法：" + ch);
        }
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException("模拟收款金额必须大于 0：" + payNo);
        }
        // value 存「渠道|金额」，查单时拆出；一笔聚合收款只对应一个渠道
        stringRedisTemplate.opsForValue()
                .set(MOCK_PAID_KEY_PREFIX + payNo, ch + "|" + amount.toPlainString(),
                        MOCK_PAID_TTL_HOURS, TimeUnit.HOURS);
        log.warn("[聚合收款·模拟] payNo={} channel={} amount={} 已登记为渠道实收（仅模拟，不作资金核对依据）",
                payNo, PayChannelEnum.getLabelOrUnknown(ch), amount.toPlainString());
    }

    /**
     * 查单：模拟模式下按登记结果判定是否已实收；没登记过 = 患者确实还没付（「等待支付」而非「查单失败」）。
     * 真实模式直接返回 null（收款事实只来自网关通知）。
     */
    public AggPaidInfo queryMockPaid(String payNo) {
        if (!isMockEnabled()) {
            return null;
        }
        String raw = stringRedisTemplate.opsForValue().get(MOCK_PAID_KEY_PREFIX + payNo);
        if (!TextUtil.hasText(raw)) {
            return null;
        }
        int sep = raw.indexOf('|');
        if (sep < 0) {
            return null;
        }
        try {
            Integer ch = Integer.valueOf(raw.substring(0, sep));
            BigDecimal amt = new BigDecimal(raw.substring(sep + 1));
            return new AggPaidInfo(ch, amt);
        } catch (NumberFormatException e) {
            log.warn("[聚合收款·模拟] 模拟标记格式异常 payNo={} raw={}", payNo, raw);
            return null;
        }
    }

    /**
     * 一笔聚合模拟收款的已登记信息：实际渠道 + 金额。
     */
    public record AggPaidInfo(Integer channel, BigDecimal amount) {
    }
}

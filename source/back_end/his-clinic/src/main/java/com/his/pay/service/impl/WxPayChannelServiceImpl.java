package com.his.pay.service.impl;

import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.pay.config.WxPayProperties;
import com.his.pay.service.WxPayChannelService;
import com.his.pay.vo.PayOrderVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 微信支付出口。
 *
 * <p>收款台出的<b>聚合码</b>不经过本通道出码：一张码多平台可扫，渠道在患者支付那一刻才确定，
 * 所以本通道只承担「查单 / 判已收 / 模拟收款」这三件与渠道对话的事。
 *
 * <p><b>真通道未接通</b>：Native 下单（{@code POST /v3/pay/transactions/native}）与 V3 回调验签解密
 * 都还没实现，缺的是商户号 + APIv3 密钥 + 商户私钥证书。模拟模式下的行为与支付宝对齐 ——
 * 到账只认显式登记的模拟收款，这样查单、入账整条链路在没有商户凭据时也能跑通并验收；
 * 真模式一律明确报错，绝不静默成功。
 */
@Service
public class WxPayChannelServiceImpl implements WxPayChannelService {

    private static final Logger log = LoggerFactory.getLogger(WxPayChannelServiceImpl.class);

    /**
     * 模拟收款标记（Redis key 前缀）：查单只认在这里显式登记过的支付单。
     */
    private static final String MOCK_PAID_KEY_PREFIX = "his:pay:mock:wechat:paid:";

    /**
     * 模拟收款标记的存活时长（小时），与一张码在窗口前的合理停留时间同量级。
     */
    private static final int MOCK_PAID_TTL_HOURS = 2;

    /**
     * 微信 V3 的「钱已收到」状态原文（协议字段，不是本库码值）。
     * 只有 SUCCESS 表示支付成功；REFUND 是退款后的状态，钱已经退回，不能再当成实收。
     */
    private static final Set<String> PAID_TRADE_STATUSES = Set.of("SUCCESS");

    private final WxPayProperties wxPayProperties;

    private final StringRedisTemplate stringRedisTemplate;

    public WxPayChannelServiceImpl(WxPayProperties wxPayProperties, StringRedisTemplate stringRedisTemplate) {
        this.wxPayProperties = wxPayProperties;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public PayUnifiedResult unifiedOrder(PayOrderVO order) {
        if (wxPayProperties.isMockEnabled()) {
            throw new BusinessException(
                    "微信支付处于模拟模式（wxpay.mock-enabled=true），无法调用真实支付。" +
                            "如需测试真实支付，请在 application-dev.yml 中配置微信商户凭据并将 mock-enabled 改为 false。"
            );
        }

        try {
            log.info("[微信支付] 调微信统一下单（V3 JSAPI） payNo={} amount={}",
                    order.getPayNo(), order.getAmount());

            // 真实场景需要：
            // 1. 从用户上下文取 openid
            // 2. 调用 wxPayService.unifiedOrderV3(...)
            // 3. 用返回的 prepay_id 做前端签名（appId/timeStamp/nonceStr/package/signType/paySign）

            log.warn("[微信支付] JSAPI 未接通（缺 openid + 前端签名逻辑）payNo={}", order.getPayNo());
            return PayUnifiedResult.fail("微信扫码收款暂未接通（缺用户 openid 与商户凭据），请改用支付宝出码");
        } catch (Exception e) {
            log.error("[微信支付] 下单失败 payNo={} err={}", order.getPayNo(), e.getMessage(), e);
            return PayUnifiedResult.fail(e.getMessage());
        }
    }

    @Override
    public TradeState queryTrade(String payNo) {
        if (!wxPayProperties.isMockEnabled()) {
            throw new BusinessException(
                    "微信查单通道未接通（缺商户 API 证书与 V3 实现），无法确认收款结果：" + payNo);
        }
        String mockAmount = stringRedisTemplate.opsForValue().get(MOCK_PAID_KEY_PREFIX + payNo);
        if (!TextUtil.hasText(mockAmount)) {
            // 没登记过模拟收款 = 患者确实还没付，这是「等待支付」而不是「查单失败」
            return null;
        }
        log.warn("[微信查单·模拟] payNo={} 按模拟收款判为已实收 amount={}（真模式请核对渠道流水）",
                payNo, mockAmount);
        return new TradeState("SIMU-WX-" + payNo, "SUCCESS", new BigDecimal(mockAmount));
    }

    @Override
    public boolean isPaidStatus(String tradeStatus) {
        return TextUtil.hasText(tradeStatus) && PAID_TRADE_STATUSES.contains(tradeStatus);
    }

    @Override
    public void markMockPaid(String payNo, BigDecimal amount) {
        if (!wxPayProperties.isMockEnabled()) {
            throw new BusinessException("微信当前是真实模式（wxpay.mock-enabled=false），拒绝模拟收款：" + payNo);
        }
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException("模拟收款金额必须大于 0：" + payNo);
        }
        stringRedisTemplate.opsForValue().set(MOCK_PAID_KEY_PREFIX + payNo, amount.toPlainString(),
                MOCK_PAID_TTL_HOURS, TimeUnit.HOURS);
        log.warn("[微信模拟收款] payNo={} amount={} 已登记为渠道实收（仅模拟模式，不作资金核对依据）",
                payNo, amount.toPlainString());
    }
}

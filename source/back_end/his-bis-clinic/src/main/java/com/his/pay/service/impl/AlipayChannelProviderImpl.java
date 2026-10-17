package com.his.pay.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.his.common.enums.PaymentMethodEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.pay.config.AlipayProperties;
import com.his.api.vo.PayOrderVO;
import com.his.api.enums.PayOrderBizTypeEnum;
import com.his.api.service.AlipayChannelProvider;
import com.his.api.service.PayOrderProvider;
import com.his.api.service.WxPayChannelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 支付宝支付出口（支付宝 SDK 真实现）
 */
@Service
public class AlipayChannelProviderImpl implements AlipayChannelProvider {
    private static final Logger log = LoggerFactory.getLogger(AlipayChannelProviderImpl.class);

    private static final String CHARSET = "UTF-8";
    private static final String SIGN_TYPE = "RSA2";

    /**
     * 模拟模式出的码内容前缀：不是任何可访问的地址，手机扫出来只会是「无法打开」，
     * 这样即使有人误把模拟模式当真实收款用，钱也进不来。
     */
    private static final String MOCK_QR_PREFIX = "MOCKALIPAY://";

    /**
     * 模拟收款标记（Redis key 前缀）：查单只认在这里显式登记过的支付单。
     */
    private static final String MOCK_PAID_KEY_PREFIX = "his:pay:mock:alipay:paid:";

    /**
     * 模拟收款标记的存活时长（小时），与一张码在窗口前的合理停留时间同量级。
     */
    private static final int MOCK_PAID_TTL_HOURS = 2;

    /**
     * 已出码串的存放 key 前缀（按支付单号）：同一张单在有效期内问渠道要几次都是同一句话，
     * 出过一次就该记住，让「刷新收款码」这类动作只花本地画图的钱。
     */
    private static final String PRECREATE_QR_KEY_PREFIX = "his:pay:alipay:qr:";

    /**
     * 码串存活时长（分钟）：比有效期多留 1 分钟。缓存先于支付单过期，等于让一次正常刷新白打渠道一趟。
     */
    private static final int PRECREATE_QR_TTL_MINUTES = PayOrderProvider.PAY_ORDER_EXPIRE_MINUTES + 1;

    /**
     * 渠道侧「钱已收到」的状态原文（协议字段，不是本库码值）：
     * TRADE_SUCCESS 可退款，TRADE_FINISHED 已过退款期，两者都已实收，漏判后者会让超期后才到账的单永远不入账。
     */
    private static final Set<String> PAID_TRADE_STATUSES = Set.of("TRADE_SUCCESS", "TRADE_FINISHED");

    private final AlipayProperties alipayProperties;

    private final StringRedisTemplate stringRedisTemplate;

    public AlipayChannelProviderImpl(AlipayProperties alipayProperties, StringRedisTemplate stringRedisTemplate) {
        this.alipayProperties = alipayProperties;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public WxPayChannelProvider.PayUnifiedResult unifiedOrder(PayOrderVO order) {
        assertRealMode("下单");
        // 客户端在 try 外构造：私钥没配是要让调用方看见的配置错，不能被下面的兜底 catch 洗成「下单失败」
        AlipayClient client = alipayClient();

        try {
            log.info("[支付宝支付] 调支付宝统一下单（alipay.trade.app.pay） payNo={} amount={}",
                    order.getPayNo(), order.getAmount());

            AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
            com.alibaba.fastjson.JSONObject bizContent = new com.alibaba.fastjson.JSONObject();
            bizContent.put("out_trade_no", order.getPayNo());
            bizContent.put("total_amount", NumUtil.plain(order.getAmount()));
            bizContent.put("subject", PayOrderBizTypeEnum.getText(order.getBizType()));
            bizContent.put("product_code", "QUICK_MSECURITY_PAY");
            request.setBizContent(bizContent.toString());
            // 不传这个参数，支付宝根本不知道该往哪推支付结果（配置里的 notify-url 只是本端记录，
            // 除非在开放平台控制台另配「默认异步通知地址」，否则通知永远不来，且零报错）
            if (TextUtil.hasText(alipayProperties.getNotifyUrl())) {
                request.setNotifyUrl(alipayProperties.getNotifyUrl());
            }

            AlipayTradeAppPayResponse response = client.sdkExecute(request);

            if (!response.isSuccess()) {
                return WxPayChannelProvider.PayUnifiedResult.fail(
                        "支付宝下单失败: " + response.getSubMsg()
                );
            }

            Map<String, String> payParams = Map.of("orderInfo", response.getBody());
            log.info("[支付宝支付] 下单成功 payNo={}", order.getPayNo());
            return WxPayChannelProvider.PayUnifiedResult.cashier(payParams);

        } catch (Exception e) {
            log.error("[支付宝支付] 下单失败 payNo={} err={}", order.getPayNo(), e.getMessage(), e);
            return WxPayChannelProvider.PayUnifiedResult.fail("支付宝下单失败：" + e.getMessage());
        }
    }

    @Override
    public WxPayChannelProvider.PayUnifiedResult precreate(PayOrderVO order) {
        if (alipayProperties.isMockEnabled()) {
            // 模拟模式给出码放行、给查单留闸：整条「出码 → 轮询 → 入账 → 流水」可以在没有渠道凭据时跑起来，
            // 但钱不会因此自己到账，必须由 markMockPaid 显式登记
            log.warn("[支付宝出码·模拟] payNo={} amount={} 出的是占位码，手机扫不开、渠道无此交易",
                    order.getPayNo(), order.getAmount());
            String mockQrCode = MOCK_QR_PREFIX + order.getPayNo();
            cacheQrCode(order.getPayNo(), mockQrCode);
            return WxPayChannelProvider.PayUnifiedResult.cashier(Map.of("qrCode", mockQrCode));
        }
        AlipayClient client = alipayClient();

        try {
            AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", order.getPayNo());
            bizContent.put("total_amount", NumUtil.plain(order.getAmount()));
            bizContent.put("subject", PayOrderBizTypeEnum.getText(order.getBizType()));
            bizContent.put("timeout_express", PayOrderProvider.PAY_ORDER_EXPIRE_MINUTES + "m");
            request.setBizContent(bizContent.toString());
            // 主扫是收费台出的码，钱到账后要记在账单上，所以推给收费侧的回调地址，不是患者端那个
            if (TextUtil.hasText(alipayProperties.getNotifyUrlCashier())) {
                request.setNotifyUrl(alipayProperties.getNotifyUrlCashier());
            }

            AlipayTradePrecreateResponse response = client.execute(request);
            if (!response.isSuccess()) {
                log.warn("[支付宝出码] 预下单失败 payNo={} subCode={} subMsg={}",
                        order.getPayNo(), response.getSubCode(), response.getSubMsg());
                return WxPayChannelProvider.PayUnifiedResult.fail("支付宝出码失败: " + response.getSubMsg());
            }
            log.info("[支付宝出码] 成功 payNo={} amount={}", order.getPayNo(), order.getAmount());
            cacheQrCode(order.getPayNo(), response.getQrCode());
            return WxPayChannelProvider.PayUnifiedResult.cashier(Map.of("qrCode", response.getQrCode()));
        } catch (Exception e) {
            // SDK 把「私钥格式不对」这类配置错包成 RuntimeException 抛出，只 catch AlipayApiException 就会漏成
            // 500「系统内部错误」——用户看不出是自己配错了，只会以为医院系统坏了
            log.error("[支付宝出码] 调用失败 payNo={} err={}", order.getPayNo(), e.getMessage(), e);
            return WxPayChannelProvider.PayUnifiedResult.fail("支付宝出码失败：" + e.getMessage());
        }
    }

    @Override
    public String recallQrCode(String payNo) {
        if (!TextUtil.hasText(payNo)) {
            return null;
        }
        try {
            return stringRedisTemplate.opsForValue().get(PRECREATE_QR_KEY_PREFIX + payNo);
        } catch (Exception e) {
            // 缓存读失败不能把出码顶死：退回真出码一次即可，代价是多一趟渠道请求，
            // 而报「出码失败」会让收银员以为系统坏了、当场停下收款
            log.warn("[支付宝出码] 码串缓存读取失败，退回真出码 payNo={} err={}", payNo, e.getMessage());
            return null;
        }
    }

    /**
     * 记住这张单的码串。写失败只留 warn：最坏结果是下次刷新真的去打一趟渠道，不值得为缓存打断收款。
     */
    private void cacheQrCode(String payNo, String qrCode) {
        if (!TextUtil.hasText(qrCode)) {
            return;
        }
        try {
            stringRedisTemplate.opsForValue()
                    .set(PRECREATE_QR_KEY_PREFIX + payNo, qrCode, PRECREATE_QR_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("[支付宝出码] 码串缓存写入失败 payNo={} err={}", payNo, e.getMessage());
        }
    }

    @Override
    public boolean closeTrade(String payNo) {
        if (alipayProperties.isMockEnabled()) {
            // 模拟模式没有渠道侧交易可关，本地单关掉就够了，不该因此把界面上的关闭动作报错退回
            log.info("[支付宝关单] 模拟模式跳过 payNo={}", payNo);
            return true;
        }
        try {
            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", payNo);
            request.setBizContent(bizContent.toString());

            AlipayTradeCloseResponse response = alipayClient().execute(request);
            if (!response.isSuccess()) {
                // 「交易不存在」是正常结果：码开出来但没人扫，渠道侧根本没有这笔交易
                log.warn("[支付宝关单] 渠道未关成 payNo={} subCode={} subMsg={}",
                        payNo, response.getSubCode(), response.getSubMsg());
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("[支付宝关单] 调用失败 payNo={} err={}", payNo, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean verifyNotify(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            log.warn("[支付宝回调] 空通知，丢弃");
            return false;
        }
        String payNo = params.get("out_trade_no");
        if (!TextUtil.hasText(params.get("sign"))) {
            log.warn("[支付宝回调] 通知缺 sign 字段，丢弃 payNo={}", payNo);
            return false;
        }

        boolean signed;
        try {
            signed = AlipaySignature.rsaCheckV1(params, alipayProperties.getAlipayPublicKey(), CHARSET, SIGN_TYPE);
        } catch (AlipayApiException e) {
            log.error("[支付宝回调] 验签异常 payNo={} err={}", payNo, e.getMessage(), e);
            return false;
        }
        if (!signed) {
            log.warn("[支付宝回调] 验签不通过，丢弃 payNo={} signType={}", payNo, params.get("sign_type"));
            return false;
        }

        // 验签只证明「支付宝用它自己的私钥签过」，不证明这单是本应用的。
        // 同一份沙箱密钥下别的 app_id 的通知照样能签过，放过就是拿别人的收款给自己的单入账。
        String notifyAppId = params.get("app_id");
        if (!Objects.equals(notifyAppId, alipayProperties.getAppId())) {
            log.warn("[支付宝回调] app_id 与本应用不符，丢弃 payNo={} notifyAppId={}", payNo, notifyAppId);
            return false;
        }
        return true;
    }

    @Override
    public TradeState queryTrade(String payNo) {
        if (alipayProperties.isMockEnabled()) {
            String mockAmount = stringRedisTemplate.opsForValue().get(MOCK_PAID_KEY_PREFIX + payNo);
            if (!TextUtil.hasText(mockAmount)) {
                // 没登记过模拟收款 = 患者确实还没付，这是「等待支付」而不是「查单失败」
                return null;
            }
            log.warn("[支付宝查单·模拟] payNo={} 按模拟收款判为已实收 amount={}（真模式请核对渠道流水）", payNo, mockAmount);
            return new TradeState(mockTradeNo(payNo), "TRADE_SUCCESS", new BigDecimal(mockAmount));
        }
        AlipayClient client = alipayClient();

        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", payNo);
            request.setBizContent(bizContent.toString());

            AlipayTradeQueryResponse response = client.execute(request);
            if (!response.isSuccess()) {
                log.warn("[支付宝查单] 未取到交易 payNo={} subCode={} subMsg={}",
                        payNo, response.getSubCode(), response.getSubMsg());
                return null;
            }
            BigDecimal totalAmount = TextUtil.hasText(response.getTotalAmount())
                    ? new BigDecimal(response.getTotalAmount()) : null;
            return new TradeState(response.getTradeNo(), response.getTradeStatus(), totalAmount);
        } catch (Exception e) {
            // 「没查成」不能报成「没有这笔交易」：轮询侧会把配置错、网络断读成「患者还没付」，
            // 界面一路显示「等待支付」而钱其实早就进渠道了
            log.error("[支付宝查单] 调用失败 payNo={} err={}", payNo, e.getMessage(), e);
            throw new BusinessException("支付宝查单失败：" + e.getMessage());
        }
    }

    @Override
    public boolean isPaidStatus(String tradeStatus) {
        return TextUtil.hasText(tradeStatus) && PAID_TRADE_STATUSES.contains(tradeStatus);
    }

    @Override
    public void markMockPaid(String payNo, BigDecimal amount) {
        if (!alipayProperties.isMockEnabled()) {
            throw new BusinessException("支付宝当前是真实模式（alipay.mock-enabled=false），拒绝模拟收款：" + payNo);
        }
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException("模拟收款金额必须大于 0：" + payNo);
        }
        stringRedisTemplate.opsForValue().set(MOCK_PAID_KEY_PREFIX + payNo, amount.toPlainString(),
                MOCK_PAID_TTL_HOURS, TimeUnit.HOURS);
        log.warn("[支付宝模拟收款] payNo={} amount={} 已登记为渠道实收（仅模拟模式，不作资金核对依据）",
                payNo, amount.toPlainString());
    }

    /**
     * 模拟交易号：与柜面扫码在本地流水上用的拼法一致，所以「渠道账单 ↔ 本地流水」的勾对能真命中，
     * 而 SIMU- 前缀一眼表明它不是商户平台给出的号。
     */
    private String mockTradeNo(String payNo) {
        return "SIMU-" + PaymentMethodEnum.ALIPAY.getCode() + "-"
                + LocalDate.now().format(DateFormats.COMPACT_DATE) + "-" + payNo;
    }

    private AlipayClient alipayClient() {
        if (!TextUtil.hasText(alipayProperties.getPrivateKey())) {
            throw new BusinessException(
                    "支付宝应用私钥未配置，无法签名。请在 gitignored 的 application-dev.yml 的 alipay.private-key " +
                            "填入开放平台密钥工具生成的应用私钥（PKCS#8 Base64，形如 MIIEvQIBADANBgkq...），" +
                            "并确认 alipay.mock-enabled=false。"
            );
        }
        return new DefaultAlipayClient(
                alipayProperties.getGatewayUrl(),
                alipayProperties.getAppId(),
                alipayProperties.getPrivateKey(),
                "json", CHARSET,
                alipayProperties.getAlipayPublicKey(),
                SIGN_TYPE
        );
    }

    private void assertRealMode(String action) {
        if (alipayProperties.isMockEnabled()) {
            throw new BusinessException(
                    "支付宝支付处于模拟模式（alipay.mock-enabled=true），无法" + action + "。" +
                    "如需" + action + "，请在 application-dev.yml 中配置支付宝凭据并将 mock-enabled 改为 false。"
            );
        }
    }
}

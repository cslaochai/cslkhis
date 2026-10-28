package com.his.charge.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.his.common.enums.PayOrderBizTypeEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.charge.config.AlipayProperties;
import com.his.charge.vo.PayOrderVO;
import com.his.charge.service.AlipayChannelService;
import com.his.charge.service.PayChannelResult.PayUnifiedResult;
import com.his.charge.service.PayChannelResult.TradeState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 支付宝支付出口（支付宝 SDK 真实现）
 */
@Service
public class AlipayChannelServiceImpl implements AlipayChannelService {
    private static final Logger log = LoggerFactory.getLogger(AlipayChannelServiceImpl.class);

    private static final String CHARSET = "UTF-8";
    private static final String SIGN_TYPE = "RSA2";

    /**
     * 渠道侧「钱已收到」的状态原文（协议字段，不是本库码值）：
     * TRADE_SUCCESS 可退款，TRADE_FINISHED 已过退款期，两者都已实收，漏判后者会让超期后才到账的单永远不入账。
     */
    private static final Set<String> PAID_TRADE_STATUSES = Set.of("TRADE_SUCCESS", "TRADE_FINISHED");

    private final AlipayProperties alipayProperties;

    public AlipayChannelServiceImpl(AlipayProperties alipayProperties) {
        this.alipayProperties = alipayProperties;
    }

    @Override
    public PayUnifiedResult unifiedOrder(PayOrderVO order) {
        assertEnabled("下单");
        // 客户端在 try 外构造：私钥没配是要让调用方看见的配置错，不能被下面的兜底 catch 洗成「下单失败」
        AlipayClient client = alipayClient();

        try {
            log.info("[支付宝支付] 调支付宝统一下单（alipay.trade.app.pay） payNo={} amount={}",
                    order.getPayNo(), order.getAmount());

            AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
            com.alibaba.fastjson.JSONObject bizContent = new com.alibaba.fastjson.JSONObject();
            bizContent.put("out_trade_no", order.getPayNo());
            bizContent.put("total_amount", NumUtil.plain(order.getAmount()));
            bizContent.put("subject", PayOrderBizTypeEnum.getLabel(order.getBizType()));
            bizContent.put("product_code", "QUICK_MSECURITY_PAY");
            request.setBizContent(bizContent.toString());
            // 不传这个参数，支付宝根本不知道该往哪推支付结果（配置里的 notify-url 只是本端记录，
            // 除非在开放平台控制台另配「默认异步通知地址」，否则通知永远不来，且零报错）
            if (TextUtil.hasText(alipayProperties.getNotifyUrl())) {
                request.setNotifyUrl(alipayProperties.getNotifyUrl());
            }

            AlipayTradeAppPayResponse response = client.sdkExecute(request);

            if (!response.isSuccess()) {
                return PayUnifiedResult.fail(
                        "支付宝下单失败: " + response.getSubMsg()
                );
            }

            Map<String, String> payParams = Map.of("orderInfo", response.getBody());
            log.info("[支付宝支付] 下单成功 payNo={}", order.getPayNo());
            return PayUnifiedResult.cashier(payParams);

        } catch (Exception e) {
            log.error("[支付宝支付] 下单失败 payNo={} err={}", order.getPayNo(), e.getMessage(), e);
            return PayUnifiedResult.fail("支付宝下单失败：" + e.getMessage());
        }
    }

    @Override
    public boolean closeTrade(String payNo) {
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

    private AlipayClient alipayClient() {
        if (!TextUtil.hasText(alipayProperties.getPrivateKey())) {
            throw new BusinessException(
                    "支付宝应用私钥未配置，无法签名。请在 gitignored 的 application-dev.yml 的 alipay.private-key " +
                            "填入开放平台密钥工具生成的应用私钥（PKCS#8 Base64，形如 MIIEvQIBADANBgkq...），" +
                            "并确认 alipay.enabled=true。"
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

    private void assertEnabled(String action) {
        if (!alipayProperties.isEnabled()) {
            throw new BusinessException(
                    "支付宝支付未启用（alipay.enabled=false），无法" + action + "。" +
                    "如需启用，请在 gitignored 的 application-dev.yml 中将 alipay.enabled 改为 true 并配置凭据。"
            );
        }
    }
}

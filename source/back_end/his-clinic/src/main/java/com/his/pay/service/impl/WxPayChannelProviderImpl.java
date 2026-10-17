package com.his.pay.service.impl;

import com.his.common.exception.BusinessException;
import com.his.pay.config.WxPayProperties;
import com.his.provider.vo.PayOrderVO;
import com.his.provider.service.WxPayChannelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 微信支付出口（JSAPI 待补全）
 */
@Service
public class WxPayChannelProviderImpl implements WxPayChannelProvider {

    private static final Logger log = LoggerFactory.getLogger(WxPayChannelProviderImpl.class);
    private final WxPayProperties wxPayProperties;

    public WxPayChannelProviderImpl(WxPayProperties wxPayProperties) {
        this.wxPayProperties = wxPayProperties;
    }

    @Override
    public PayUnifiedResult unifiedOrder(PayOrderVO order) {
        if (wxPayProperties.isMockEnabled()) {
            throw new BusinessException(
                    "微信支付处于模拟模式（wx.pay.mock-enabled=true），无法调用真实支付。" +
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
    public PayUnifiedResult nativePrecreate(PayOrderVO order) {
        if (wxPayProperties.isMockEnabled()) {
            throw new BusinessException(
                    "微信支付处于模拟模式（wx.pay.mock-enabled=true），无法调用真实支付。" +
                            "如需测试真实支付，请在 application-dev.yml 中配置微信商户凭据并将 mock-enabled 改为 false"
            );
        }
        // 未接通要说清缺什么并返回失败，不能静默判成功：
        // 静默成功等于界面显示「已收款」而渠道一分钱没收，钱和账两头都不存在
        log.warn("[微信支付] Native 出码未接通（缺商户 API 证书与 V3 下单实现）payNo={}", order.getPayNo());
        return PayUnifiedResult.fail("微信 Native 扫码收款暂未接通（商户 API 证书未配置），请改用支付宝出码");
    }
}

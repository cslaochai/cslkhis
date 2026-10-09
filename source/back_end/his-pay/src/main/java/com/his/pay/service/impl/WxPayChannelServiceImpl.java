package com.his.pay.service.impl;

import com.his.common.config.pay.WxPayProperties;
import com.his.pay.entity.BizPayOrder;
import com.his.pay.service.WxPayChannelService;
import com.his.pay.service.WxPayChannelService.PayUnifiedResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 微信支付出口（JSAPI 待补全）
 */
@Service
public class WxPayChannelServiceImpl implements WxPayChannelService {

    private static final Logger log = LoggerFactory.getLogger(WxPayChannelServiceImpl.class);
    private final WxPayProperties wxPayProperties;

    public WxPayChannelServiceImpl(WxPayProperties wxPayProperties) {
        this.wxPayProperties = wxPayProperties;
    }

    @Override
    public PayUnifiedResult unifiedOrder(BizPayOrder order) {
        if (wxPayProperties.isMockEnabled()) {
            throw new IllegalStateException(
                    "微信支付处于模拟模式（wx.pay.mock-enabled=true），无法调用真实支付。" +
                            "如需测试真实支付，请在 application-local.yml 中配置微信商户凭据并将 mock-enabled 改为 false。"
            );
        }

        try {
            log.info("[微信支付] 调微信统一下单（V3 JSAPI） payNo={} amount={}",
                    order.getPayNo(), order.getAmount());

            // TODO: 完整 JSAPI 下单需要 openid + 前端签名，当前先留桩
            // 真实场景需要：
            // 1. 从用户上下文取 openid
            // 2. 调用 wxPayService.unifiedOrderV3(...)
            // 3. 用返回的 prepay_id 做前端签名（appId/timeStamp/nonceStr/package/signType/paySign）

            log.warn("[微信支付] JSAPI 完整实现待补：需要 openid + 前端签名逻辑");
            return PayUnifiedResult.fail("JSAPI 支付暂未完整实现，请先用模拟模式");
        } catch (Exception e) {
            log.error("[微信支付] 下单失败 payNo={} err={}", order.getPayNo(), e.getMessage(), e);
            return PayUnifiedResult.fail(e.getMessage());
        }
    }
}

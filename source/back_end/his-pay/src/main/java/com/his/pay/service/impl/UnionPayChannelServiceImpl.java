package com.his.pay.service.impl;

import com.his.common.config.pay.UnionPayProperties;
import com.his.pay.entity.BizPayOrder;
import com.his.pay.service.UnionPayChannelService;
import com.his.pay.service.WxPayChannelService;
import com.his.pay.service.WxPayChannelService.PayUnifiedResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 云闪付支付出口（待接 SDK）
 */
@Service
public class UnionPayChannelServiceImpl implements UnionPayChannelService {
    private static final Logger log = LoggerFactory.getLogger(UnionPayChannelServiceImpl.class);

    private final UnionPayProperties unionPayProperties;

    public UnionPayChannelServiceImpl(UnionPayProperties unionPayProperties) {
        this.unionPayProperties = unionPayProperties;
    }

    @Override
    public WxPayChannelService.PayUnifiedResult unifiedOrder(BizPayOrder order) {
        if (unionPayProperties.isMockEnabled()) {
            throw new IllegalStateException(
                    "云闪付支付处于模拟模式（unionpay.mock-enabled=true），无法调用真实支付。" +
                    "如需测试真实支付，请在 application-local.yml 中配置云闪付凭据并将 mock-enabled 改为 false。"
            );
        }

        try {
            log.info("[云闪付支付] 调云闪付统一下单 payNo={} amount={}",
                    order.getPayNo(), order.getAmount());

            // TODO: 云闪付 SDK 接入（需要银联开放平台 appId + merId + 密钥）
            log.warn("[云闪付支付] SDK 完整实现待补");
            return WxPayChannelService.PayUnifiedResult.fail("云闪付支付暂未完整实现");

        } catch (Exception e) {
            log.error("[云闪付支付] 下单失败 payNo={} err={}", order.getPayNo(), e.getMessage(), e);
            return WxPayChannelService.PayUnifiedResult.fail(e.getMessage());
        }
    }
}

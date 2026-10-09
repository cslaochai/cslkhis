package com.his.pay.service;

import com.his.pay.entity.BizPayOrder;

import java.util.Map;

/**
 * 支付宝支付出口
 */
public interface AlipayChannelService {

    WxPayChannelService.PayUnifiedResult unifiedOrder(BizPayOrder order);
}

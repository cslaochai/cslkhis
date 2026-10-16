package com.his.pay.service;

import com.his.api.service.WxPayChannelProvider;
import com.his.pay.entity.BizPayOrder;

/**
 * 云闪付支付出口
 */
public interface UnionPayChannelService {

    WxPayChannelProvider.PayUnifiedResult unifiedOrder(BizPayOrder order);
}

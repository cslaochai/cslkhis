package com.his.pay.service;

import com.his.pay.service.WxPayChannelService;
import com.his.pay.entity.BizPayOrder;

/**
 * 云闪付支付出口
 */
public interface UnionPayChannelService {

    WxPayChannelService.PayUnifiedResult unifiedOrder(BizPayOrder order);
}

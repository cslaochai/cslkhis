package com.his.charge.service;

import com.his.charge.service.WxPayChannelService;
import com.his.charge.entity.BizPayOrder;

/**
 * 云闪付支付出口
 */
public interface UnionPayChannelService {

    WxPayChannelService.PayUnifiedResult unifiedOrder(BizPayOrder order);
}

package com.his.charge.service;

import com.his.charge.entity.BizPayOrder;
import com.his.charge.service.PayChannelResult.PayUnifiedResult;

/**
 * 云闪付支付出口
 */
public interface UnionPayChannelService {

    PayUnifiedResult unifiedOrder(BizPayOrder order);
}

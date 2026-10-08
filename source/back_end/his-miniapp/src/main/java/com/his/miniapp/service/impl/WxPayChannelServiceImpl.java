package com.his.miniapp.service.impl;

import com.his.miniapp.entity.BizPayOrder;
import com.his.miniapp.enums.PayOrderBizTypeEnum;
import com.his.miniapp.service.WxPayChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 微信支付出口
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class WxPayChannelServiceImpl implements WxPayChannelService {

    /**
     * 统一下单。
     */
    public PayUnifiedResult unifiedOrder(BizPayOrder order) {
        log.info("[微信支付口子] ===== 模拟调微信统一下单（V3 transactions/jsapi）=====");
        log.info("[微信支付口子] 商户单号={} 业务类型={} 业务单ID={} 金额=￥{} 描述={}",
                order.getPayNo(), PayOrderBizTypeEnum.getText(order.getBizType()), order.getBizId(),
                order.getAmount(), PayOrderBizTypeEnum.getText(order.getBizType()));
        log.info("[微信支付口子] ===== 模拟支付回调（notify 验签通过）=====");
        log.info("[微信支付口子] out_trade_no={} trade_state=SUCCESS transaction_id=MOCK_{}",
                order.getPayNo(), System.currentTimeMillis());
        return PayUnifiedResult.mock();
    }
}
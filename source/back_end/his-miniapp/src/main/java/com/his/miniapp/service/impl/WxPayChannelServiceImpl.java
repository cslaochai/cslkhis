package com.his.miniapp.service.impl;

import com.his.miniapp.enums.PayBizTypeEnum;
import com.his.miniapp.service.WxPayChannelService;
import com.his.miniapp.entity.BizPayOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 微信支付出口（小程序一期口子，同 M7/M8 打印桩形态）。
 *
 * <p>标准链路：业务下单落患者端统一支付单(待支付) → 统一下单 →
 * 前端 {@code wx.requestPayment(payParams)} 拉收银台 → 微信异步回调
 * {@code /miniapp/pay/notify} → 验签 → 推进业务。当前为打印桩：打印下单参数与回调报文，
 * 直接返回"支付成功"，骨架与业务推进完全不变。真实对接 = 在本类内改走 V3 下单 + 平台证书验签。
 */
@Slf4j
@Service
public class WxPayChannelServiceImpl implements WxPayChannelService {

    /**
     * 统一下单。
     *
     * @param order 已落库的支付单（待支付状态）
     * @return mockPaid=true 表示桩模式（调用方应立即推进支付成功）；
     *         false 表示真收银台模式，payParams 交给前端 wx.requestPayment
     */
    public PayUnifiedResult unifiedOrder(BizPayOrder order) {
        log.info("[微信支付口子] ===== 模拟调微信统一下单（V3 transactions/jsapi）=====");
        log.info("[微信支付口子] 商户单号={} 业务类型={} 业务单ID={} 金额=￥{} 描述={}",
                order.getPayNo(), PayBizTypeEnum.labelOrUnknown(order.getBizType()), order.getBizId(),
                order.getAmount(), PayBizTypeEnum.labelOrUnknown(order.getBizType()));
        log.info("[微信支付口子] ===== 模拟支付回调（notify 验签通过）=====");
        log.info("[微信支付口子] out_trade_no={} trade_state=SUCCESS transaction_id=MOCK_{}",
                order.getPayNo(), System.currentTimeMillis());
        return PayUnifiedResult.mock();
    }
}

package com.his.spi.service;

import com.his.spi.dto.ChannelNotifyRecordDTO;
import com.his.spi.vo.ChannelTradeStateVO;
import com.his.spi.vo.MicroPayResultVO;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付渠道端口：收款台与收费入账要用到的渠道侧动作（柜面刷付款码、通知验签、主动查单、通知留痕）。
 *
 * <p>收款事实只从这一层拿：渠道说收到才算收到，业务侧不许自己判「算不算已付」。
 */
public interface PayChannelFacadeService {

    /**
     * 付款码收款：刷患者手机上出示的付款码，向渠道要一次实收结果。
     * 用于收费收银台「扫码收款」提交时写收款流水之前的那一步渠道确认。
     *
     * @param authCode 付款码（扫码枪读出的字符串）
     * @param orderNo  商户订单号（对账时两边勾得上）
     * @param amount   金额（元）
     * @param subject  商品描述
     * @param channel  收款渠道（1-微信 2-支付宝）
     * @return 渠道成败与渠道交易号；失败绝不误报成成功
     */
    MicroPayResultVO microPay(String authCode, String orderNo, BigDecimal amount, String subject, Integer channel);

    /**
     * 支付宝异步通知验签：只回答「这条通知是不是支付宝签的、是不是发给本应用的」，
     * 不判断该不该入账。用于收费台收款码的支付宝回调入口。
     */
    boolean verifyAlipayNotify(Map<String, String> params);

    /**
     * 支付宝交易状态是否「钱已实收」。状态原文属渠道协议，业务侧只问结论。
     */
    boolean isAlipayPaidStatus(String tradeStatus);

    /**
     * 支付宝主动查单：回调断了也能把已付的钱问回来。用于收款结果轮询的兜底入账。
     *
     * @return 渠道明确回答「无此交易/尚未付款」时返回 null；调用本身失败（配置、签名、网络）抛业务异常
     */
    ChannelTradeStateVO queryAlipayTrade(String payNo);

    /**
     * 微信交易状态是否「钱已实收」。
     */
    boolean isWxPaidStatus(String tradeStatus);

    /**
     * 微信主动查单。用于收款结果轮询的兜底入账。
     *
     * @return 渠道明确回答「无此交易/尚未付款」时返回 null；调用本身失败一律抛业务异常，
     * 不许把「查不了」说成「患者还没付」
     */
    ChannelTradeStateVO queryWxTrade(String payNo);

    /**
     * 落一条渠道通知留痕。用于回调出口在应答渠道之前先记下「收到了什么、怎么处理到哪一步」。
     * 实现方保证不抛异常：留痕是旁路证据，写不进去不该打断已经收到钱的入账链路。
     */
    void recordChannelNotify(ChannelNotifyRecordDTO dto);
}

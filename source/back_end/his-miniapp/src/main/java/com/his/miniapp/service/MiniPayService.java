package com.his.miniapp.service;

import com.his.miniapp.dto.PayRefundDTO;
import com.his.miniapp.dto.PayUpsertDTO;
import com.his.miniapp.vo.MiniPayOrderListVO;
import com.his.miniapp.vo.MiniPayOrderVO;
import com.his.miniapp.vo.MiniPendingBillListVO;

import java.util.List;
import java.util.Map;

/**
 * 患者端支付服务：统一支付单。
 */
public interface MiniPayService {

    /**
     * 下单：建支付单并向渠道要收银台参数（payParams 交前端拉起）。
     * 本方法不置支付成功 —— 收款事实只由异步通知或 {@link #syncFromChannel} 查单确认。
     */
    MiniPayOrderVO createOrder(PayUpsertDTO dto);

    /**
     * 我的支付单（患者维度，最近 50 条）
     */
    List<MiniPayOrderListVO> myOrders();

    /**
     * 我的待缴账单（含明细，四层结算账单口径）：已取代旧的 /miniapp/charge/pendingPage。
     * 返回 patientId 下未付清（待支付 / 部分支付）的结算账单，按出账倒序。
     */
    List<MiniPendingBillListVO> pendingBills();

    /**
     * 按业务单退款（已支付 → 已退款，打印原路退回）
     */
    void refundByBiz(PayRefundDTO dto);

    /**
     * 支付宝异步通知入账：验签 → 单据归属 → 金额逐字比对 → 条件翻转 → 业务推进。
     * 任何一步不通过都不入账。
     *
     * @return 渠道协议应答文本：`success`=已受理（含重复通知与「未付款」通知），
     * `failure`=未受理，渠道按重试策略再推
     */
    String confirmByAlipayNotify(Map<String, String> params);

    /**
     * 微信支付异步通知。通道未接通，任何通知都不受理（返回非应答码，让商户平台的失败计数暴露出来）。
     */
    String confirmByWxNotify(String body);

    /**
     * 向渠道查单并兜底入账 —— 异步通知丢失、公网回调不可达时，收款确认的唯一通路。
     * 只允许查本人支付单；已是已支付状态则原样返回。
     */
    MiniPayOrderVO syncFromChannel(String payNo);
}

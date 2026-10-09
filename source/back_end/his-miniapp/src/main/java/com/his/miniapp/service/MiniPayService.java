package com.his.miniapp.service;

import com.his.miniapp.dto.PayRefundDTO;
import com.his.miniapp.dto.PayUpsertDTO;
import com.his.miniapp.vo.MiniPayOrderListVO;
import com.his.miniapp.vo.MiniPayOrderVO;
import com.his.miniapp.vo.MiniPendingBillListVO;

import java.util.List;

/**
 * 患者端支付服务：统一支付单。
 */
public interface MiniPayService {

    /**
     * 下单支付。模式直接推进支付成功并触发业务推进（门诊缴费走收费执行器、
     * 押金走预交金充值、挂号费打印入账）；真收银台模式返回 payParams 由前端拉起。
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
}

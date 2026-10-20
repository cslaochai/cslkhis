package com.his.miniapp.controller;

import com.his.common.base.Result;
import com.his.miniapp.dto.PayRefundDTO;
import com.his.miniapp.dto.PayUpsertDTO;
import com.his.miniapp.service.MiniPayService;
import com.his.miniapp.vo.MiniPayOrderListVO;
import com.his.miniapp.vo.MiniPayOrderVO;
import com.his.miniapp.vo.MiniPendingBillListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者端统一支付
 */
@Tag(name = "患者端-支付")
@RestController
@RequestMapping("/miniapp/pay")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PATIENT')")
public class MiniPayController {

    private final MiniPayService miniPayService;

    /**
     * 下单支付：按业务单建患者端支付单并发起收款，桩模式直接置为已支付，真收银台模式回吐调起参数。
     * 用于小程序挂号缴费、门诊缴费与住院押金充值的确认支付动作。
     */
    @Operation(summary = "下单支付（模式直接返回已支付；真收银台模式返回 payParams）")
    @PostMapping("/createOrder")
    public Result<MiniPayOrderVO> createOrder(@RequestBody @Valid PayUpsertDTO dto) {
        return Result.success("支付成功", miniPayService.createOrder(dto));
    }

    /**
     * 我的支付单：当前登录患者最近的支付记录，含渠道与到账状态。
     * 供支付结果自查与缴费记录展示；当前小程序页面尚未接该取数。
     */
    @Operation(summary = "我的支付单（最近50条）")
    @GetMapping("/myOrders")
    public Result<List<MiniPayOrderListVO>> myOrders() {
        return Result.success(miniPayService.myOrders());
    }

    /**
     * 按业务单退款：把该业务单名下已支付的款项原路退回，无支付单时幂等跳过。
     * 供退号退费等「业务撤销联动退款」环节回写资金；退号动作由挂号侧发起，本入口留给其他业务线复用。
     */
    @Operation(summary = "按业务单退款（退号退费；无已支付单幂等跳过）")
    @PostMapping("/refund")
    public Result<Void> refund(@RequestBody @Valid PayRefundDTO dto) {
        miniPayService.refundByBiz(dto);
        return Result.success("退款已受理", null);
    }

    /**
     * 渠道查单兜底入账：主动向支付渠道核对这笔支付单的状态，已支付但本地未落账时补记账。
     * 用于异步通知丢失或公网回调不可达时的补偿环节，只允许查本人支付单。
     */
    @Operation(summary = "向渠道查单并兜底入账（异步通知丢失/公网回调不可达时用；只查本人支付单）")
    @PostMapping("/syncFromChannel")
    public Result<MiniPayOrderVO> syncFromChannel(@RequestParam String payNo) {
        return Result.success(miniPayService.syncFromChannel(payNo));
    }

    /**
     * 我的待缴账单：列出结算口径下尚未缴清的账单及其费用明细。
     * 用于小程序「门诊缴费」页的待缴清单。
     */
    @Operation(summary = "我的待缴账单（含明细，四层结算账单口径；取代旧 /miniapp/charge/pendingPage）")
    @PostMapping("/pendingBills")
    public Result<List<MiniPendingBillListVO>> pendingBills() {
        return Result.success(miniPayService.pendingBills());
    }
}

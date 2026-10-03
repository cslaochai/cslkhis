package com.his.miniapp.controller;

import com.his.common.base.Result;
import com.his.miniapp.dto.PayUpsertDTO;
import com.his.miniapp.dto.PayRefundDTO;
import com.his.miniapp.service.MiniappPayService;
import com.his.miniapp.vo.PayOrderListVO;
import com.his.miniapp.vo.PayOrderVO;
import com.his.miniapp.vo.PendingBillListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者端统一支付（微信支付口子：患者端统一支付单落单 → 统一下单 → 桩模式直接推进/真模式返回收银台参数）。
 */
@Tag(name = "患者端-支付")
@RestController
@RequestMapping("/miniapp/pay")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PATIENT')")
public class MiniappPayController {

    private final MiniappPayService miniappPayService;

    @Operation(summary = "下单支付（桩模式直接返回已支付；真收银台模式返回 payParams）")
    @PostMapping("/createOrder")
    public Result<PayOrderVO> createOrder(@RequestBody @Valid PayUpsertDTO dto) {
        return Result.success("支付成功", miniappPayService.createOrder(dto));
    }

    @Operation(summary = "我的支付单（最近50条）")
    @GetMapping("/myOrders")
    public Result<List<PayOrderListVO>> myOrders() {
        return Result.success(miniappPayService.myOrders());
    }

    @Operation(summary = "按业务单退款（退号退费；无已支付单幂等跳过）")
    @PostMapping("/refund")
    public Result<Void> refund(@RequestBody @Valid PayRefundDTO dto) {
        miniappPayService.refundByBiz(dto);
        return Result.success("退款已受理", null);
    }

    @Operation(summary = "我的待缴账单（含明细，四层结算账单口径；取代旧 /miniapp/charge/pendingPage）")
    @PostMapping("/pendingBills")
    public Result<List<PendingBillListVO>> pendingBills() {
        return Result.success(miniappPayService.pendingBills());
    }
}

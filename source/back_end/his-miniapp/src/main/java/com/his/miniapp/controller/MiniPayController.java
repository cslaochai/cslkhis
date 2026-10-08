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

    @Operation(summary = "下单支付（模式直接返回已支付；真收银台模式返回 payParams）")
    @PostMapping("/createOrder")
    public Result<MiniPayOrderVO> createOrder(@RequestBody @Valid PayUpsertDTO dto) {
        return Result.success("支付成功", miniPayService.createOrder(dto));
    }

    @Operation(summary = "我的支付单（最近50条）")
    @GetMapping("/myOrders")
    public Result<List<MiniPayOrderListVO>> myOrders() {
        return Result.success(miniPayService.myOrders());
    }

    @Operation(summary = "按业务单退款（退号退费；无已支付单幂等跳过）")
    @PostMapping("/refund")
    public Result<Void> refund(@RequestBody @Valid PayRefundDTO dto) {
        miniPayService.refundByBiz(dto);
        return Result.success("退款已受理", null);
    }

    @Operation(summary = "我的待缴账单（含明细，四层结算账单口径；取代旧 /miniapp/charge/pendingPage）")
    @PostMapping("/pendingBills")
    public Result<List<MiniPendingBillListVO>> pendingBills() {
        return Result.success(miniPayService.pendingBills());
    }
}

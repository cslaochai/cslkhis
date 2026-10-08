package com.his.charge.controller;


import com.his.charge.dto.PaymentTxnQueryPageDTO;
import com.his.charge.service.PaymentService;
import com.his.charge.vo.BizPaymentTxnVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 支付资金流水台账（L3）。
 */
@Tag(name = "支付流水")
@RestController
@RequestMapping("/charge/paymentTxn")
@RequiredArgsConstructor
public class PaymentTxnController {

    private final PaymentService paymentService;

    @Operation(summary = "分页查询支付流水")
    @PreAuthorize("hasAuthority('finance:payTxn:list')")
    @PostMapping("/listPage")
    public Result<PageResult<BizPaymentTxnVO>> listPage(@Valid @RequestBody PaymentTxnQueryPageDTO query) {
        return Result.success(paymentService.selectPage(query));
    }

    @Operation(summary = "某张账单的全部收/退流水")
    @PreAuthorize("hasAuthority('finance:payTxn:list')")
    @GetMapping("/listByBill")
    public Result<List<BizPaymentTxnVO>> listByBill(@RequestParam Long billId) {
        return Result.success(paymentService.listByBillVO(billId));
    }
}
